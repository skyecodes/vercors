package app.vercors.launcher.common.data.instance

import app.vercors.launcher.common.data.AppPath
import app.vercors.launcher.common.data.DefaultDispatcher
import app.vercors.launcher.common.data.IODispatcher
import app.vercors.launcher.common.data.Sha1
import app.vercors.launcher.common.domain.LAUNCHER_NAME
import app.vercors.launcher.common.domain.LAUNCHER_VERSION
import app.vercors.launcher.common.domain.account.model.Account
import app.vercors.launcher.common.domain.instance.LauncherService
import app.vercors.launcher.common.domain.instance.model.Instance
import app.vercors.launcher.common.domain.instance.model.InstanceLaunchFeature
import app.vercors.launcher.common.domain.instance.model.InstanceLaunchState
import app.vercors.launcher.common.domain.system.OsType
import app.vercors.launcher.common.domain.system.SystemService
import app.vercors.shared.domain.game.GameRepository
import app.vercors.shared.domain.game.model.GameVersionDetails
import app.vercors.shared.domain.game.model.GameVersionType
import app.vercors.shared.domain.getOrThrow
import de.jonasbroeckmann.kzip.Zip
import de.jonasbroeckmann.kzip.extractTo
import de.jonasbroeckmann.kzip.open
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.operations.Hasher
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.io.buffered
import kotlinx.io.bytestring.toHexString
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.koin.core.annotation.Single

private const val GAME_ASSET_DOWNLOAD_URL = "https://resources.download.minecraft.net"

private val logger = KotlinLogging.logger {}

@Single
class LauncherServiceImpl(
    private val gameRepository: GameRepository,
    private val systemService: SystemService,
    private val httpClient: HttpClient,
    @AppPath private val appPath: Path,
    @Sha1 private val sha1Hasher: Hasher,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : LauncherService {
    override fun launch(
        instance: Instance,
        account: Account,
        features: List<InstanceLaunchFeature>
    ): Flow<InstanceLaunchState> = flow {
        logger.info { "Launching instance ${instance.name}..." }
        val version = gameRepository.getVersionDetails(instance.gameVersion).getOrThrow() // TODO save it to disk
        val effectiveVersion = version.getEffectiveVersion(features)
        val paths = instance.buildPaths(appPath)
        val dependencyList = effectiveVersion.buildDependencyList(paths)
        dependencyList.validate()
        dependencyList.natives.extract()
        val command = buildCommand(dependencyList, account, instance, effectiveVersion, paths)
        runCommand(command, paths.game, defaultDispatcher)
        logger.info { "Instance ${instance.name} finished" }
    }

    private suspend fun buildCommand(
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
        }.joinToString(
            separator = platformPathSeparator,
            prefix = "\"",
            postfix = "\""
        )
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
        var args = buildList {
            add("java")
            add("-Djava.library.path=${paths.natives}")
            logConfigArg?.let { add(it) }
            jvmArgs?.let { addAll(it) }
            gameArgs?.let {
                add("-cp")
                add(classpath)
            }
            add(version.mainClass)
            gameArgs?.let { addAll(it) }
        }
        if (systemService.osType == OsType.Linux) {
            // For some reason, the command cannot be executed on Linux, so we have to use a temporary script file instead
            val tmpDir = Path(appPath, "tmp")
            val file = Path(tmpDir, "run-${instance.id}.sh")
            withContext(ioDispatcher) {
                SystemFileSystem.createDirectories(tmpDir)
                SystemFileSystem.sink(file).buffered().use {
                    it.writeText(args.joinToString(" "))
                }
            }
            args = listOf("/bin/sh", SystemFileSystem.resolve(file).toString())
        }
        return args
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
        val instancePath = Path(appPath, "instances", id)
        return InstanceLaunchPaths(
            game = instancePath,
            versions = Path(appPath, "versions"),
            libraries = Path(appPath, "libraries"),
            assets = Path(appPath, "assets"),
            natives = Path(instancePath, "natives"),
            logging = Path(appPath, "logging")
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

    private fun GameVersionDetails.buildDependencyList(paths: InstanceLaunchPaths): InstanceDependencyList =
        InstanceDependencyList(
            client = DependencyFile.Basic(
                url = clientDownload.url,
                path = Path(paths.versions, "${id}.jar"),
                sha1 = clientDownload.sha1,
                size = clientDownload.size,
            ),
            libraries = libraries.mapNotNull { it.artifact }.map {
                DependencyFile.Basic(
                    url = it.url,
                    path = Path(paths.libraries, *it.path.splitPath()),
                    sha1 = it.sha1,
                    size = it.size,
                )
            },
            natives = libraries.mapNotNull { lib ->
                lib.classifiers?.get(lib.natives?.get(systemService.osType.id))?.let {
                    DependencyFile.Native(
                        url = it.url,
                        path = Path(paths.libraries, *it.path.splitPath()),
                        sha1 = it.sha1,
                        size = it.size,
                        extractDest = paths.natives,
                        extractExclude = lib.extractExclude
                    )
                }
            },
            assets = assets.map {
                DependencyFile.Basic(
                    url = "$GAME_ASSET_DOWNLOAD_URL/${it.hash.take(2)}/${it.hash}",
                    path = Path(paths.assets, "objects", it.hash.take(2), it.hash),
                    sha1 = it.hash,
                    size = it.size,
                )
            },
            logging = clientLogging?.file?.let {
                DependencyFile.Basic(
                    url = it.url,
                    path = Path(paths.logging, clientLogging!!.type.path, it.id),
                    sha1 = it.sha1,
                    size = it.size,
                )
            },
        )

    private suspend fun InstanceDependencyList.validate() {
        logger.info { "Validating dependencies..." }
        var attempts = 0
        var invalidFiles = allDependencies.getInvalidFiles()
        while (attempts++ < 3 && invalidFiles.isNotEmpty()) {
            logger.info { "${invalidFiles.size} invalid or missing dependencies to be downloaded, attempt $attempts" }
            invalidFiles.downloadFiles()
            invalidFiles = invalidFiles.getInvalidFiles()
        }
        check(invalidFiles.isEmpty()) { "Failed to validate dependencies" }
        logger.info { "Dependencies validated" }
    }

    private suspend fun List<DependencyFile.Native>.extract() = coroutineScope {
        if (isEmpty()) return@coroutineScope
        logger.info { "Extracting natives..." }
        val extractDispatcher = defaultDispatcher + SupervisorJob()
        map { file ->
            launch(extractDispatcher) {
                logger.debug { "Extracting ${file.path} to ${file.extractDest}..." }
                Zip.open(file.path).use { zip ->
                    zip.extractTo(file.extractDest)
                    /*zip.forEachEntry { entry ->
                        val entryPath = entry.path.toString()
                        if (file.extractExclude?.any { entryPath.startsWith(it) } ?: false) {
                            SystemFileSystem.delete(Path(file.extractDest, entryPath))
                        }
                    }*/
                }
                logger.debug { "Extracted ${file.path} to ${file.extractDest}" }
            }
        }.joinAll()
        logger.info { "Natives extracted" }
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

    sealed interface DependencyFile {
        val url: String
        val path: Path
        val sha1: String
        val size: Long

        data class Basic(
            override val url: String,
            override val path: Path,
            override val sha1: String,
            override val size: Long
        ) : DependencyFile

        data class Native(
            override val url: String,
            override val path: Path,
            override val sha1: String,
            override val size: Long,
            val extractDest: Path,
            val extractExclude: List<String>?
        ) : DependencyFile
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

    private suspend fun List<DependencyFile>.getInvalidFiles() = coroutineScope {
        val validateDispatcher = defaultDispatcher + SupervisorJob()
        map {
            async(validateDispatcher) {
                it to it.validate()
            }
        }.awaitAll()
            .filter { (_, result) -> result != null }
            .map { (file, _) -> file }
    }

    private suspend fun List<DependencyFile>.downloadFiles() = coroutineScope {
        val downloadDispatcher = ioDispatcher.limitedParallelism(10) + SupervisorJob()
        map {
            launch(downloadDispatcher) {
                logger.debug { "Downloading ${it.path}..." }
                runBlocking {
                    SystemFileSystem.createDirectories(it.path.parent!!)
                    httpClient.get(it.url)
                        .bodyAsChannel()
                        .copyAndClose(SystemFileSystem.sink(it.path).asByteWriteChannel())
                }
                logger.debug { "Downloaded ${it.path}" }
            }
        }.joinAll()
    }

    @OptIn(DelicateCryptographyApi::class)
    private suspend fun DependencyFile.validate(): DependencyCheckError? {
        logger.debug { "Validating $path..." }
        val metadata = SystemFileSystem.metadataOrNull(path)
            ?: return DependencyCheckError.Missing
        if (size != metadata.size)
            return DependencyCheckError.SizeMismatch
        if (sha1 != SystemFileSystem.source(path).use { sha1Hasher.hash(it) }.toHexString())
            return DependencyCheckError.HashMismatch
        logger.debug { "Validated $path" }
        return null
    }

    private enum class DependencyCheckError {
        Missing,
        SizeMismatch,
        HashMismatch
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
        "assets_index_name" to version.assetsId,
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

internal expect suspend fun runCommand(
    command: List<String>,
    workingDir: Path,
    dispatcher: CoroutineDispatcher
)
