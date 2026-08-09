package app.vercors.feature.launcher.data.service

import app.vercors.core.filesystem.createDirectories
import app.vercors.core.system.SystemService
import app.vercors.feature.account.domain.model.Account
import app.vercors.feature.game.domain.model.GameAssetIndex
import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.game.domain.model.GameVersionType
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.launcher.domain.model.InstanceLaunchEvent
import app.vercors.feature.launcher.domain.model.InstanceLaunchFeature
import app.vercors.feature.launcher.domain.service.LauncherService
import app.vercors.shared.data.FileValidationFailure
import app.vercors.shared.data.FileValidationRequest
import app.vercors.shared.data.FileValidationService
import app.vercors.shared.domain.LAUNCHER_NAME
import app.vercors.shared.domain.LAUNCHER_VERSION
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
    private val appPath: Path,
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
        val effectiveVersion = version.getEffectiveVersion(features)
        val paths = instance.buildPaths(appPath)
        val dependencyList = buildDependencyList(paths, effectiveVersion, assetIndex)
        either { dependencyList.validate() }.onLeft { failures ->
            logger.error { "Failed to validate dependencies for instance ${instance.name}: ${failures.size} invalid files" }
            logger.debug { "Invalid files: ${failures.joinToString(", ") { it.toString() }}" }
            return@channelFlow
        }
        dependencyList.natives.extract()
        val command = buildCommand(dependencyList, account, instance, effectiveVersion, paths)
        paths.game.createDirectories()
        val runJob = launch { runCommand(command, paths.game, effectiveVersion.clientLogging?.type) }
        send(InstanceLaunchEvent.Running { runJob.cancel() })
        runJob.join()
        logger.info { "Instance ${instance.name} finished" }
    }

    private fun GameVersionDetails.getEffectiveVersion(features: List<InstanceLaunchFeature>): GameVersionDetails {
        val enabledFeatures = features.map { it.id }
        return copy(
            libraries = libraries.filter { it.rules.isRuleValid(enabledFeatures) },
            jvmArgs = jvmArgs?.filter { it.isRuleValid(enabledFeatures) },
            gameArgs = gameArgs?.filter { it.isRuleValid(enabledFeatures) }
        )
    }

    private fun Instance.buildPaths(appPath: Path): InstanceLaunchPaths {
        val dataPath = Path(appPath, "data")
        val instancePath = Path(dataPath, "instances", id)
        return InstanceLaunchPaths(
            game = instancePath,
            versions = Path(dataPath, "versions"),
            libraries = Path(dataPath, "libraries"),
            assets = Path(dataPath, "assets"),
            natives = Path(instancePath, "natives"),
            logging = Path(dataPath, "logging")
        )
    }

    private data class InstanceLaunchPaths(
        val game: Path,
        val versions: Path,
        val libraries: Path,
        val assets: Path,
        val natives: Path,
        val logging: Path
    )

    private fun buildDependencyList(
        paths: InstanceLaunchPaths,
        details: GameVersionDetails,
        assetIndex: GameAssetIndex
    ): InstanceDependencyList =
        InstanceDependencyList(
            client = DependencyFile.Basic(
                url = details.clientDownload.url,
                path = Path(paths.versions, details.id, "${details.id}.jar"),
                size = details.clientDownload.size,
                sha1 = details.clientDownload.sha1,
            ),
            libraries = details.libraries.mapNotNull { it.artifact }.map {
                DependencyFile.Basic(
                    url = it.url,
                    path = Path(paths.libraries, *it.path.splitPath()),
                    size = it.size,
                    sha1 = it.sha1,
                )
            },
            natives = details.libraries.mapNotNull { lib ->
                lib.classifiers?.get(lib.natives?.get(systemService.osType.id))?.let {
                    DependencyFile.Native(
                        url = it.url,
                        path = Path(paths.libraries, *it.path.splitPath()),
                        size = it.size,
                        sha1 = it.sha1,
                        extractDest = paths.natives,
                        extractExclude = lib.extractExclude
                    )
                }
            },
            assets = assetIndex.objects.values.map {
                DependencyFile.Basic(
                    url = "$GAME_ASSET_DOWNLOAD_URL/${it.hash.take(2)}/${it.hash}",
                    path = Path(paths.assets, "objects", it.hash.take(2), it.hash),
                    size = it.size,
                    sha1 = it.hash,
                )
            },
            logging = details.clientLogging?.file?.let {
                DependencyFile.Basic(
                    url = it.url,
                    path = Path(paths.logging, details.clientLogging!!.type.path, it.id),
                    size = it.size,
                    sha1 = it.sha1,
                )
            },
        )

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
        version: GameVersionDetails,
        paths: InstanceLaunchPaths
    ): List<String> {
        val classpath = buildList {
            addAll(dependencyList.libraries.map { it.path.toString() })
            add(dependencyList.client.path.toString())
            add(".")
        }.joinToString(separator = platformPathSeparator)
        val argVariables = buildArgVariables(
            account = account,
            instance = instance,
            version = version,
            classpath = classpath,
            gamePath = paths.game,
            assetsPath = paths.assets,
            nativesPath = paths.natives,
        )
        val jvmArgs = version.jvmArgs?.resolve(argVariables)
        val gameArgs = version.gameArgs?.resolve(argVariables)
        val logConfigArg = version.clientLogging?.argument?.replaceVars(
            variables = mapOf("path" to dependencyList.logging!!.path.toString())
        )
        return buildList {
            add("java")
            logConfigArg?.let { add(it) }
            jvmArgs?.let { addAll(it) } ?: run {
                add("-Djava.library.path=${paths.natives}")
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

    private fun List<GameVersionDetails.Rule>?.isRuleValid(features: List<String>): Boolean =
        this == null || all { it.isRuleValid(features) }

    private fun GameVersionDetails.Rule.isRuleValid(features: List<String>): Boolean {
        val applies = this.features.isRuleValid(features) && os.isRuleValid()
        return when (action) {
            GameVersionDetails.Rule.Action.Allow -> applies
            GameVersionDetails.Rule.Action.Disallow -> !applies
        }
    }

    private fun Map<String, Boolean>?.isRuleValid(features: List<String>): Boolean =
        this == null || entries.all { (feature, value) -> feature in features == value }

    private fun GameVersionDetails.Rule.Os?.isRuleValid(): Boolean {
        if (this == null)
            return true
        if (!name.isNullOrEmpty() && systemService.osType.id != name)
            return false
        if (!version.isNullOrEmpty() && !systemService.osVersion.matches(version!!.toRegex()))
            return false
        if (!arch.isNullOrEmpty() && systemService.osArch.id != arch)
            return false
        return true
    }

    private fun GameVersionDetails.Argument.isRuleValid(features: List<String>): Boolean = when (this) {
        is GameVersionDetails.Argument.Basic -> true
        is GameVersionDetails.Argument.Conditional -> rules.all { it.isRuleValid(features) }
    }

    private fun String.splitPath(): Array<String> = split("/").filter { it.isNotBlank() }.toTypedArray()

    private val GameVersionDetails.Logging.Type.path: String
        get() = when (this) {
            GameVersionDetails.Logging.Type.LOG4J2_XML -> "log4j2-xml"
        }

    private fun buildArgVariables(
        account: Account,
        instance: Instance,
        version: GameVersionDetails,
        classpath: String,
        gamePath: Path,
        assetsPath: Path,
        nativesPath: Path,
    ): Map<String, String> = mapOf(
        "auth_player_name" to account.username,
        "version_name" to instance.gameVersion,
        "game_directory" to gamePath.toString(),
        "assets_root" to assetsPath.toString(),
        "assets_index_name" to version.assets,
        "auth_uuid" to account.id.toString(),
        "auth_access_token" to account.accessToken,
        "clientid" to "",
        "auth_xuid" to "",
        "user_type" to "msa",
        "version_type" to version.type.value,
        "natives_directory" to nativesPath.toString(),
        "launcher_name" to LAUNCHER_NAME,
        "launcher_version" to LAUNCHER_VERSION,
        "classpath" to classpath,
        // Legacy
        "auth_session" to account.accessToken,
        "user_properties" to "{}",
        "uuid" to account.id.toString(),
        "accessToken" to account.accessToken,
        "game_assets" to assetsPath.toString()
    )

    private val GameVersionType.value: String
        get() = when (this) {
            GameVersionType.Release -> "release"
            GameVersionType.Snapshot -> "snapshot"
            GameVersionType.Beta -> "old_beta"
            GameVersionType.Alpha -> "old_alpha"
        }

    private fun List<GameVersionDetails.Argument>.resolve(variables: Map<String, String>): List<String> = flatMap {
        when (it) {
            is GameVersionDetails.Argument.Basic -> listOf(it.value)
            is GameVersionDetails.Argument.Conditional -> it.value
        }
    }.map { it.replaceVars(variables) }

    private fun String.replaceVars(variables: Map<String, String>) = variables.entries.fold(this) { acc, (key, value) ->
        acc.replace($$"${$$key}", value)
    }
}

internal expect val platformPathSeparator: String

internal expect suspend fun ProducerScope<InstanceLaunchEvent>.runCommand(
    command: List<String>,
    workingDir: Path,
    loggingType: GameVersionDetails.Logging.Type?,
    context: CoroutineContext = Dispatchers.IO
)
