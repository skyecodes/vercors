package app.vercors.feature.launcher.data.service

import app.vercors.core.filesystem.createDirectories
import app.vercors.core.system.SystemService
import app.vercors.feature.account.domain.model.Account
import app.vercors.feature.game.domain.model.GameAssetIndex
import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.launcher.domain.model.InstanceLaunchEvent
import app.vercors.feature.launcher.domain.model.InstanceLaunchFeature
import app.vercors.feature.launcher.domain.model.LaunchVariables
import app.vercors.feature.launcher.domain.service.LaunchRuleEvaluator
import app.vercors.feature.launcher.domain.service.LauncherService
import app.vercors.feature.launcher.domain.service.resolve
import app.vercors.feature.launcher.domain.service.substituteVariables
import app.vercors.shared.data.AppPaths
import app.vercors.shared.data.FileValidationFailure
import app.vercors.shared.data.FileValidationRequest
import app.vercors.shared.data.FileValidationService
import arrow.core.NonEmptyList
import arrow.core.raise.Raise
import arrow.core.raise.either
import de.jonasbroeckmann.kzip.Zip
import de.jonasbroeckmann.kzip.extractTo
import de.jonasbroeckmann.kzip.forEachEntry
import de.jonasbroeckmann.kzip.open
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.coroutines.CoroutineContext

private const val GAME_ASSET_DOWNLOAD_URL = "https://resources.download.minecraft.net"

private val logger = KotlinLogging.logger {}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class LauncherServiceImpl(
    private val systemService: SystemService,
    private val appPaths: AppPaths,
    private val ruleEvaluator: LaunchRuleEvaluator,
    private val fileValidationService: FileValidationService
) : LauncherService {
    override fun launch(
        instance: Instance,
        account: Account,
        version: GameVersionDetails,
        assetIndex: GameAssetIndex,
        features: List<InstanceLaunchFeature>,
    ): Flow<InstanceLaunchEvent> = channelFlow {
        logger.info { "Launching instance ${instance.name}..." }
        val effectiveVersion = ruleEvaluator.applyRules(version, features)
        val dependencyList = buildDependencyList(instance.id, effectiveVersion, assetIndex)
        either { dependencyList.validate() }.onLeft { failures ->
            logger.error { "Failed to validate dependencies for instance ${instance.name}: ${failures.size} invalid files" }
            logger.debug { "Invalid files: ${failures.joinToString(", ") { it.toString() }}" }
            return@channelFlow
        }
        dependencyList.natives.extract()
        val command = buildCommand(dependencyList, account, instance, effectiveVersion)
        val gameDir = appPaths.gameDir(instance.id)
        gameDir.createDirectories()
        val runJob = launch { runCommand(command, gameDir, effectiveVersion.clientLogging?.type) }
        send(InstanceLaunchEvent.Running { runJob.cancel() })
        runJob.join()
        logger.info { "Instance ${instance.name} finished" }
    }

    private fun buildDependencyList(
        instanceId: InstanceId,
        details: GameVersionDetails,
        assetIndex: GameAssetIndex
    ): InstanceDependencyList {
        val nativesDir = appPaths.nativesDir(instanceId)
        return InstanceDependencyList(
            client = DependencyFile.Basic(
                url = details.clientDownload.url,
                path = appPaths.versionJarFile(details.id),
                size = details.clientDownload.size,
                sha1 = details.clientDownload.sha1,
            ),
            libraries = details.libraries.mapNotNull { it.artifact }.map {
                DependencyFile.Basic(
                    url = it.url,
                    path = appPaths.libraryFile(it.path),
                    size = it.size,
                    sha1 = it.sha1,
                )
            },
            natives = details.libraries.mapNotNull { lib ->
                lib.classifiers?.get(lib.natives?.get(systemService.osType.id))?.let {
                    DependencyFile.Native(
                        url = it.url,
                        path = appPaths.libraryFile(it.path),
                        size = it.size,
                        sha1 = it.sha1,
                        extractDest = nativesDir,
                        extractExclude = lib.extractExclude
                    )
                }
            },
            assets = assetIndex.objects.values.map {
                DependencyFile.Basic(
                    url = "$GAME_ASSET_DOWNLOAD_URL/${it.hash.take(2)}/${it.hash}",
                    path = appPaths.assetObjectFile(it.hash),
                    size = it.size,
                    sha1 = it.hash,
                )
            },
            logging = details.clientLogging?.let { clientLogging ->
                DependencyFile.Basic(
                    url = clientLogging.file.url,
                    path = appPaths.loggingConfigFile(clientLogging.type.dirName, clientLogging.file.id),
                    size = clientLogging.file.size,
                    sha1 = clientLogging.file.sha1,
                )
            },
        )
    }

    context(_: ProducerScope<InstanceLaunchEvent>, _: Raise<NonEmptyList<FileValidationFailure>>)
    private suspend fun InstanceDependencyList.validate() {
        logger.info { "Validating dependencies..." }
        fileValidationService.submit(allDependencies) {

        }
        logger.info { "Dependencies validated" }
    }

    private suspend fun List<DependencyFile.Native>.extract() = coroutineScope {
        if (isEmpty()) return@coroutineScope
        logger.info { "Extracting natives..." }
        val extractDispatcher = Dispatchers.Default + SupervisorJob()
        map { file ->
            launch(extractDispatcher) {
                logger.trace { "Extracting ${file.path} to ${file.extractDest}..." }
                Zip.open(file.path).use { zip ->
                    zip.extractTo(file.extractDest)
                    zip.forEachEntry { entry ->
                        val entryPath = entry.path.toString()
                        if (file.extractExclude?.any { entryPath.startsWith(it) } ?: false) {
                            SystemFileSystem.delete(Path(file.extractDest, entryPath))
                        }
                    }
                }
                logger.trace { "Extracted ${file.path} to ${file.extractDest}" }
            }
        }.joinAll()
        logger.info { "Natives extracted" }
    }

    private fun buildCommand(
        dependencyList: InstanceDependencyList,
        account: Account,
        instance: Instance,
        version: GameVersionDetails
    ): List<String> {
        val nativesDir = appPaths.nativesDir(instance.id)
        val classpath = buildList {
            addAll(dependencyList.libraries.map { it.path.toString() })
            add(dependencyList.client.path.toString())
            add(".")
        }.joinToString(separator = platformPathSeparator)
        val variables = LaunchVariables(
            account = account,
            instance = instance,
            version = version,
            classpath = classpath,
            gameDir = appPaths.gameDir(instance.id).toString(),
            assetsDir = appPaths.assets.toString(),
            nativesDir = nativesDir.toString(),
        ).toMap()
        val jvmArgs = version.jvmArgs?.resolve(variables)
        val gameArgs = version.gameArgs?.resolve(variables)
        val logConfigArg = version.clientLogging?.argument?.substituteVariables(
            variables = mapOf("path" to dependencyList.logging!!.path.toString())
        )
        return buildList {
            add("java")
            logConfigArg?.let { add(it) }
            jvmArgs?.let { addAll(it) } ?: run {
                add("-Djava.library.path=$nativesDir")
                add("-cp")
                add(classpath)
            }
            add(version.mainClass)
            gameArgs?.let { addAll(it) }
        }
    }

    private data class InstanceDependencyList(
        val client: DependencyFile,
        val libraries: List<DependencyFile>,
        val natives: List<DependencyFile.Native>,
        val assets: List<DependencyFile>,
        val logging: DependencyFile?
    ) {
        val allDependencies: List<DependencyFile> = buildList {
            add(client)
            addAll(libraries)
            addAll(natives)
            addAll(assets)
            logging?.let { add(it) }
        }
    }

    sealed class DependencyFile(
        url: String,
        path: Path,
        size: Long,
        sha1: String
    ) : FileValidationRequest.Url(url, path, size, sha1) {
        class Basic(
            url: String,
            path: Path,
            size: Long,
            sha1: String
        ) : DependencyFile(url, path, size, sha1)

        class Native(
            url: String,
            path: Path,
            size: Long,
            sha1: String,
            val extractDest: Path,
            val extractExclude: List<String>?
        ) : DependencyFile(url, path, size, sha1)
    }

    private val GameVersionDetails.Logging.Type.dirName: String
        get() = when (this) {
            GameVersionDetails.Logging.Type.LOG4J2_XML -> "log4j2-xml"
        }
}

internal expect val platformPathSeparator: String

internal expect suspend fun ProducerScope<InstanceLaunchEvent>.runCommand(
    command: List<String>,
    workingDir: Path,
    loggingType: GameVersionDetails.Logging.Type?,
    context: CoroutineContext = Dispatchers.IO
)
