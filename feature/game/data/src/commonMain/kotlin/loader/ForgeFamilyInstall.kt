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
import app.vercors.shared.data.AppPaths
import app.vercors.shared.data.FileValidationRequest
import app.vercors.shared.data.FileValidationService
import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import arrow.core.raise.context.withError
import de.jonasbroeckmann.kzip.Zip
import de.jonasbroeckmann.kzip.open
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger {}
private val PLACEHOLDER_REGEX = Regex("\\{([A-Za-z_]+)}")
private const val MAX_ERROR_OUTPUT_LENGTH = 4000

// Two concurrent installs for the same loader+version (e.g. a double-triggered launch) would otherwise race
// on the same shared processor working directory and clobber each other's output files mid-write.
private val installMutexesLock = Mutex()
private val installMutexes = mutableMapOf<String, Mutex>()

private suspend fun installMutexFor(key: String): Mutex = installMutexesLock.withLock {
    installMutexes.getOrPut(key) { Mutex() }
}

/**
 * Downloads and runs a Forge/NeoForge-family installer, dispatching on whichever installer format it turns out
 * to be (see [InstallerProfiles]): the modern, processor-based format extracts `install_profile.json` +
 * `version.json` from the installer jar, downloads the libraries both reference, resolves each processor's
 * placeholder arguments and runs it (client-side processors only), then returns the resulting profile —
 * caching it so later launches skip straight past all of this. The legacy (pre-1.13 Forge) format has no
 * processors/patching step at all — see [InstallerProfiles.Legacy]/[extractProfiles].
 */
context(_: Raise<ModLoaderInstallError>)
internal suspend fun installForgeFamily(
    loaderType: ModLoaderType,
    loaderKey: String,
    gameVersion: GameVersionDetails,
    loaderVersion: String,
    appPaths: AppPaths,
    fileValidationService: FileValidationService,
    processRunner: ProcessRunner,
    systemService: SystemService,
    json: Json
): LoaderInstallProfile = installMutexFor("$loaderKey-${gameVersion.id}-$loaderVersion").withLock {
    val profileCachePath = appPaths.loaderProfileFile(gameVersion.id, loaderKey, loaderVersion)
    cachedProfileOrNull(profileCachePath, appPaths, json)?.let { return@withLock it }

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

    when (val profiles = extractProfiles(loaderType, installerJarPath, appPaths, json)) {
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

        // Not written to profileCachePath: every launch re-extracts the already-downloaded, on-disk installer
        // jar (a small, bounded local I/O cost, no network involved) rather than caching a second profile
        // format alongside LoaderVersionProfile's modern-shaped one. Revisit if this becomes a real cost.
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

    // The maven coordinate the patched client jar ends up at isn't predictable from the loader's own
    // artifact coordinate — it's whatever install_profile.json's own "PATCHED" data entry says (this has
    // changed between Forge-family versions, e.g. newer NeoForge uses a "minecraft-client-patched" artifact
    // name instead of its own), so it must be read from there rather than guessed. It — and NeoForge's own
    // "universal" jar, downloaded above as one of install_profile's own libraries — are deliberately NOT added
    // to the classpath: FML's own production locator finds both itself by scanning "library_directory" for
    // these exact coordinates, and putting them on the classpath instead makes FML think it's running inside
    // a Gradle modding workspace rather than a normal launch.
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

/**
 * A cached profile can outlive the local-only artifacts it points to (e.g. deleted by a bug, or by the user
 * clearing part of the cache) — this returns `null` (asking the caller to re-install) rather than handing out
 * dangling references.
 */
private suspend fun cachedProfileOrNull(profileCachePath: Path, appPaths: AppPaths, json: Json): LoaderInstallProfile? {
    if (!profileCachePath.exists()) return null
    val cached = profileCachePath.readJson(LoaderVersionProfile.serializer(), json)
    return cached.takeIf { it.locallyProducedArtifactsExist(appPaths) }?.toDomain()
}

/** Whether every locally-produced artifact this profile depends on (see [LoaderVersionProfile.localArtifacts]) still exists on disk. */
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
    // The vanilla client jar is normally downloaded later, by the launch pipeline's own dependency
    // resolution — but the installer's processors need it to already be on disk to slim/split/patch it.
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

/**
 * Reads `install_profile.json` from the installer jar and, based on whether it also has a `version.json`
 * entry, decodes either the modern (processor-based) or legacy (pre-1.13 Forge) profile shape — `version.json`
 * only exists in the modern format, so its absence (checked via [Zip.entryOrNull], not by catching an
 * exception) is the confirmed, reliable signal to distinguish the two. For a legacy profile, the universal jar
 * named in `install.filePath` is extracted straight from the already-open installer jar to its library path —
 * legacy Forge never binary-patches the client jar, so unlike the modern flow, there's no separate processor
 * step needed to produce it.
 */
context(_: Raise<ModLoaderInstallError>)
private suspend fun extractProfiles(
    loaderType: ModLoaderType,
    installerJarPath: Path,
    appPaths: AppPaths,
    json: Json
): InstallerProfiles = runCatching {
    Zip.open(installerJarPath).use { zip ->
        val installProfileText = zip.entry(Path("install_profile.json")) { readToBytes() }.decodeToString()
        val versionText = zip.entryOrNull(Path("version.json")) { readToBytes() }?.decodeToString()
        if (versionText != null) {
            InstallerProfiles.Modern(
                json.decodeFromString(InstallProfileData.serializer(), installProfileText),
                json.decodeFromString(LoaderVersionProfile.serializer(), versionText)
            )
        } else {
            val legacyProfile = json.decodeFromString(LegacyForgeInstallProfile.serializer(), installProfileText)
            // versionInfo.libraries typically also lists this same coordinate (with a real-looking maven "url")
            // as if it were a normal downloadable library — it isn't; that URL 404s on the real maven. The
            // installer produces it locally instead, and every legacy Minecraft launcher places it at exactly
            // this coordinate's own (unclassified) library path, not a separate "-universal.jar" classifier —
            // toDomain() relies on that by filtering any self-referencing entry out of versionInfo.libraries
            // and substituting this locally-extracted, non-fetched one in its place.
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
}.getOrElse { e ->
    if (e is CancellationException) throw e
    logger.error(e) { "Failed to extract $loaderType installer profiles from $installerJarPath" }
    raise(ModLoaderInstallError.FetchFailed(loaderType, e.message ?: "Unable to read the installer jar"))
}

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
            // Forge's install_profile.json provides this; NeoForge's doesn't — null skips the hash
            // check instead of comparing against "" (which would never match and loop redownloads).
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
        if (e is CancellationException) throw e
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
