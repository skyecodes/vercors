package app.vercors.feature.game.data.loader

import app.vercors.core.filesystem.*
import app.vercors.core.system.OsType
import app.vercors.core.system.ProcessRunner
import app.vercors.core.system.SystemService
import app.vercors.feature.game.data.dto.InstallProfileData
import app.vercors.feature.game.data.dto.LegacyForgeInstallProfile
import app.vercors.feature.game.data.dto.LoaderVersionProfile
import app.vercors.feature.game.data.dto.MojangVersionInfo
import app.vercors.feature.game.data.toDomain
import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.game.domain.model.LoaderInstallProfile
import app.vercors.feature.game.domain.model.ModLoaderInstallError
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.domain.service.key
import app.vercors.shared.data.AppPaths
import app.vercors.shared.data.FileValidationRequest
import app.vercors.shared.data.FileValidationService
import arrow.core.raise.Raise
import arrow.core.raise.context.bind
import arrow.core.raise.context.raise
import arrow.core.raise.context.withError
import arrow.core.raise.either
import de.jonasbroeckmann.kzip.Zip
import de.jonasbroeckmann.kzip.open
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger {}
private val PLACEHOLDER_REGEX = Regex("\\{([A-Za-z_]+)}")
private const val MAX_ERROR_OUTPUT_LENGTH = 4000

@SingleIn(AppScope::class)
@Inject
class ForgeFamilyInstaller(
    private val appPaths: AppPaths,
    private val fileValidationService: FileValidationService,
    private val processRunner: ProcessRunner,
    private val systemService: SystemService,
    private val json: Json
) {
    // Concurrent installs of the same loader version would clobber the shared processor working directory
    private val installMutexesLock = Mutex()
    private val installMutexes = mutableMapOf<String, Mutex>()

    context(_: Raise<ModLoaderInstallError>)
    suspend fun install(
        loaderType: ModLoaderType,
        gameVersion: GameVersionDetails,
        loaderVersion: String
    ): LoaderInstallProfile = installMutexFor("${loaderType.key}-${gameVersion.id}-$loaderVersion").withLock {
        installForgeFamily(
            loaderType = loaderType,
            loaderKey = loaderType.key,
            gameVersion = gameVersion,
            loaderVersion = loaderVersion,
            appPaths = appPaths,
            fileValidationService = fileValidationService,
            processRunner = processRunner,
            systemService = systemService,
            json = json
        )
    }

    private suspend fun installMutexFor(key: String): Mutex = installMutexesLock.withLock {
        installMutexes.getOrPut(key) { Mutex() }
    }
}

context(_: Raise<ModLoaderInstallError>)
private suspend fun installForgeFamily(
    loaderType: ModLoaderType,
    loaderKey: String,
    gameVersion: GameVersionDetails,
    loaderVersion: String,
    appPaths: AppPaths,
    fileValidationService: FileValidationService,
    processRunner: ProcessRunner,
    systemService: SystemService,
    json: Json
): LoaderInstallProfile {
    val profileCachePath = appPaths.loaderProfileFile(gameVersion.id, loaderKey, loaderVersion)
    cachedProfileOrNull(profileCachePath, appPaths, json)?.let { return it }

    val rootDir = appPaths.versionDir("${gameVersion.id}-$loaderKey-$loaderVersion")
    val installerJarPath = appPaths.installerJarFile(loaderKey, gameVersion.id, loaderVersion)
    val installerRequest = loaderInstallArtifactRequest(
        gameVersionId = gameVersion.id,
        loaderId = loaderKey,
        loaderVersion = loaderVersion,
        path = installerJarPath
    )

    downloadInstallerJar(loaderType, installerRequest, fileValidationService)
    downloadVanillaClientJar(loaderType, gameVersion, appPaths, fileValidationService)

    return when (val profiles = extractProfiles(loaderType, installerJarPath, appPaths, json)) {
        is InstallerProfiles.Modern -> installModernForgeFamily(
            loaderType = loaderType,
            gameVersion = gameVersion,
            appPaths = appPaths,
            fileValidationService = fileValidationService,
            processRunner = processRunner,
            systemService = systemService,
            json = json,
            rootDir = rootDir,
            installerJarPath = installerJarPath,
            profileCachePath = profileCachePath,
            profiles = profiles
        )

        // Not cached: re-extracting from the local installer jar is cheap
        is InstallerProfiles.Legacy -> profiles.profile.toDomain()
    }
}

context(_: Raise<ModLoaderInstallError>)
private suspend fun installModernForgeFamily(
    loaderType: ModLoaderType,
    gameVersion: GameVersionDetails,
    appPaths: AppPaths,
    fileValidationService: FileValidationService,
    processRunner: ProcessRunner,
    systemService: SystemService,
    json: Json,
    rootDir: Path,
    installerJarPath: Path,
    profileCachePath: Path,
    profiles: InstallerProfiles.Modern
): LoaderInstallProfile {
    val (installProfile, versionProfile) = profiles
    downloadInstallProfileLibraries(loaderType, installProfile, appPaths, fileValidationService)

    val resolvedData = resolveInstallData(installProfile, installerJarPath, rootDir, appPaths)
    val knownVars = mapOf(
        "SIDE" to "client",
        "ROOT" to rootDir.toString(),
        "INSTALLER" to installerJarPath.toString(),
        "MINECRAFT_JAR" to appPaths.versionJarFile(gameVersion.id).toString(),
        "MINECRAFT_VERSION" to gameVersion.id
    )
    val classpathSeparator = if (systemService.osType == OsType.Windows) ";" else ":"
    runProcessors(
        loaderType,
        installProfile.processors,
        knownVars,
        resolvedData,
        classpathSeparator,
        rootDir,
        appPaths,
        processRunner
    )

    // Read from install_profile's PATCHED entry (it varies across versions); never on the classpath, FML scans library_directory for it
    val patchedArtifact = buildPatchedArtifact(loaderType, installProfile, resolvedData, appPaths)
    val finalProfile = versionProfile.copy(localArtifacts = listOfNotNull(patchedArtifact))
    profileCachePath.writeJson(LoaderVersionProfile.serializer(), finalProfile, json)
    return finalProfile.toDomain().copy(omitClientFromClasspath = true)
}

private sealed interface InstallerProfiles {
    data class Modern(val installProfile: InstallProfileData, val versionProfile: LoaderVersionProfile) :
        InstallerProfiles

    data class Legacy(val profile: LegacyForgeInstallProfile) : InstallerProfiles
}

// A cached profile can outlive its local artifacts: null asks the caller to re-install
private suspend fun cachedProfileOrNull(profileCachePath: Path, appPaths: AppPaths, json: Json): LoaderInstallProfile? {
    if (!profileCachePath.exists()) return null
    val cached = profileCachePath.readJson(LoaderVersionProfile.serializer(), json)
    return cached.takeIf { it.locallyProducedArtifactsExist(appPaths) }?.toDomain()
}

private suspend fun LoaderVersionProfile.locallyProducedArtifactsExist(appPaths: AppPaths): Boolean =
    localArtifacts.all { artifact ->
        val metadata = appPaths.libraryFile(artifact.path).metadataOrNull()
        metadata != null && metadata.isRegularFile && metadata.size == artifact.size
    }

context(_: Raise<ModLoaderInstallError>)
private suspend fun downloadInstallerJar(
    loaderType: ModLoaderType,
    installerRequest: FileValidationRequest,
    fileValidationService: FileValidationService
) {
    withError({ ModLoaderInstallError.FetchFailed(loaderType, it) }) {
        fileValidationService.submit(listOf(installerRequest)) {}
    }
}

context(_: Raise<ModLoaderInstallError>)
private suspend fun downloadVanillaClientJar(
    loaderType: ModLoaderType,
    gameVersion: GameVersionDetails,
    appPaths: AppPaths,
    fileValidationService: FileValidationService
) {
    // The processors need the vanilla client jar on disk before the launch pipeline would download it
    withError({ ModLoaderInstallError.FetchFailed(loaderType, it) }) {
        fileValidationService.submit(
            listOf(
                FileValidationRequest.Url(
                    url = gameVersion.clientDownload.url,
                    path = appPaths.versionJarFile(gameVersion.id),
                    size = gameVersion.clientDownload.size,
                    sha1 = gameVersion.clientDownload.sha1
                )
            )
        ) {}
    }
}

context(_: Raise<ModLoaderInstallError>)
private suspend fun extractProfiles(
    loaderType: ModLoaderType,
    installerJarPath: Path,
    appPaths: AppPaths,
    json: Json
): InstallerProfiles = runCatching {
    Zip.open(installerJarPath).use { zip ->
        either<ModLoaderInstallError, InstallerProfiles> {
            val installProfileText = zip.entry(Path("install_profile.json")) { readToBytes() }.decodeToString()
            val versionText = zip.entryOrNull(Path("version.json")) { readToBytes() }?.decodeToString()
            if (versionText != null) {
                InstallerProfiles.Modern(
                    json.decodeFromString(InstallProfileData.serializer(), installProfileText),
                    json.decodeFromString(LoaderVersionProfile.serializer(), versionText)
                )
            } else {
                val legacyProfile = json.decodeFromString(LegacyForgeInstallProfile.serializer(), installProfileText)
                // The listed URL for this coordinate 404s: extract the bundled universal jar to its library path
                val universalFile = appPaths.libraryFile(mavenCoordinateToPath(legacyProfile.install.path))
                universalFile.createParentDirectories()
                zip.entryOrNull(Path(legacyProfile.install.filePath)) { readToPath(universalFile) } ?: raise(
                    ModLoaderInstallError.FetchFailed(
                        loaderType,
                        "Universal jar entry not found in installer: ${legacyProfile.install.filePath}"
                    )
                )
                InstallerProfiles.Legacy(legacyProfile)
            }
        }
    }
}.getOrElse { e ->
    currentCoroutineContext().ensureActive()
    logger.error(e) { "Failed to extract $loaderType installer profiles from $installerJarPath" }
    raise(ModLoaderInstallError.FetchFailed(loaderType, e.message ?: "Unable to read the installer jar"))
}.bind()

context(_: Raise<ModLoaderInstallError>)
private suspend fun downloadInstallProfileLibraries(
    loaderType: ModLoaderType,
    installProfile: InstallProfileData,
    appPaths: AppPaths,
    fileValidationService: FileValidationService
) {
    withError({ ModLoaderInstallError.FetchFailed(loaderType, it) }) {
        val libraryFiles = installProfile.libraries.mapNotNull { it.downloads?.artifact }.map { artifact ->
            FileValidationRequest.Url(
                url = artifact.url,
                path = appPaths.libraryFile(artifact.path),
                size = artifact.size,
                sha1 = artifact.sha1
            )
        }
        if (libraryFiles.isNotEmpty()) fileValidationService.submit(libraryFiles) {}
    }
}

private suspend fun resolveInstallData(
    installProfile: InstallProfileData,
    installerJarPath: Path,
    rootDir: Path,
    appPaths: AppPaths
): Map<String, String> = buildMap {
    Zip.open(installerJarPath).use { zip ->
        for ((key, value) in installProfile.data) {
            put(key, zip.resolveDataValue(value.client, rootDir, appPaths))
        }
    }
}

context(_: Raise<ModLoaderInstallError>)
private suspend fun runProcessors(
    loaderType: ModLoaderType,
    processors: List<InstallProfileData.Processor>,
    knownVars: Map<String, String>,
    resolvedData: Map<String, String>,
    classpathSeparator: String,
    rootDir: Path,
    appPaths: AppPaths,
    processRunner: ProcessRunner
) {
    for (processor in processors) {
        if (processor.sides != null && "client" !in processor.sides) continue
        val jarPath = appPaths.libraryFile(mavenCoordinateToPath(processor.jar))
        val mainClass = jarPath.readMainClassOrRaise(loaderType, processor.jar)
        val classpath = (listOf(processor.jar) + processor.classpath)
            .joinToString(classpathSeparator) { appPaths.libraryFile(mavenCoordinateToPath(it)).toString() }
        val args = processor.args.map { it.resolveArg(knownVars, resolvedData, appPaths) }
        val command = listOf("java", "-cp", classpath, mainClass) + args
        val result = processRunner.run(command, workingDir = rootDir.toString())
        if (result.exitCode != 0) {
            val output = result.stderr.ifBlank { result.stdout }.takeLast(MAX_ERROR_OUTPUT_LENGTH)
            logger.error { "$loaderType processor $mainClass exited with code ${result.exitCode}:\n$output" }
            raise(ModLoaderInstallError.ProcessFailed(loaderType, mainClass, result.exitCode, output))
        }
    }
}

context(_: Raise<ModLoaderInstallError>)
private suspend fun buildPatchedArtifact(
    loaderType: ModLoaderType,
    installProfile: InstallProfileData,
    resolvedData: Map<String, String>,
    appPaths: AppPaths
): MojangVersionInfo.Library.Downloads.Artifact? =
    installProfile.data["PATCHED"]?.client?.let { rawPatched ->
        val relativePath = mavenCoordinateToPath(rawPatched.removeSurrounding("[", "]"))
        val size = appPaths.libraryFile(relativePath).metadataOrNull()?.size ?: run {
            val message = "Patched client jar was not produced at ${appPaths.libraryFile(relativePath)}"
            logger.error { message }
            raise(ModLoaderInstallError.ProcessFailed(loaderType, "binarypatcher", -1, message))
        }
        MojangVersionInfo.Library.Downloads.Artifact(
            path = relativePath,
            // NeoForge provides no hash here: null skips the check instead of comparing against ""
            sha1 = resolvedData["PATCHED_SHA"],
            size = size,
            url = ""
        )
    }

context(_: Raise<ModLoaderInstallError>)
private fun Path.readMainClassOrRaise(loaderType: ModLoaderType, processorJar: String): String {
    val result = runCatching {
        Zip.open(this).use { zip ->
            zip.entry(Path("META-INF/MANIFEST.MF")) { readToBytes() }.decodeToString()
                .lineSequence()
                .firstOrNull { it.startsWith("Main-Class:") }
                ?.removePrefix("Main-Class:")
                ?.trim()
                ?: error("No Main-Class in $this")
        }
    }
    return result.getOrElse { e ->
        logger.error(e) { "Failed to read main class from processor jar $processorJar ($this)" }
        raise(
            ModLoaderInstallError.ProcessFailed(
                loaderType,
                processorJar,
                -1,
                e.message ?: "Unable to read processor manifest"
            )
        )
    }
}

private suspend fun Zip.resolveDataValue(raw: String, rootDir: Path, appPaths: AppPaths): String = when {
    raw.startsWith("[") && raw.endsWith("]") ->
        appPaths.libraryFile(mavenCoordinateToPath(raw.removeSurrounding("[", "]"))).toString()

    raw.startsWith("'") && raw.endsWith("'") -> raw.removeSurrounding("'", "'")

    raw.startsWith("/") -> {
        val entryPath = raw.trimStart('/')
        val dest = Path(rootDir, entryPath)
        val bytes = entry(Path(entryPath)) { readToBytes() }
        dest.createParentDirectories()
        dest.sink { it.write(bytes) }
        dest.toString()
    }

    else -> raw
}

private fun String.resolveArg(
    knownVars: Map<String, String>,
    resolvedData: Map<String, String>,
    appPaths: AppPaths
): String {
    if (startsWith("[") && endsWith("]")) {
        return appPaths.libraryFile(mavenCoordinateToPath(removeSurrounding("[", "]"))).toString()
    }
    return PLACEHOLDER_REGEX.replace(this) { match ->
        val key = match.groupValues[1]
        knownVars[key] ?: resolvedData[key] ?: match.value
    }
}
