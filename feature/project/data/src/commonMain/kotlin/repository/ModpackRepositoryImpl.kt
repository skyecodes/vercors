package app.vercors.feature.project.data.repository

import app.vercors.core.filesystem.deleteRecursively
import app.vercors.core.filesystem.exists
import app.vercors.feature.instance.domain.model.localimport.isUserStatePath
import app.vercors.feature.project.data.modpack.ModpackManifestParser
import app.vercors.feature.project.data.projectFileDownloadRequest
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.domain.model.modpack.*
import app.vercors.feature.project.domain.repository.ModpackRepository
import app.vercors.shared.data.*
import arrow.core.NonEmptyList
import arrow.core.raise.Raise
import arrow.core.raise.context.ensureNotNull
import arrow.core.raise.context.raise
import arrow.core.raise.recover
import de.jonasbroeckmann.kzip.Zip
import de.jonasbroeckmann.kzip.forEachEntry
import de.jonasbroeckmann.kzip.open
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.*
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import opensavvy.progress.Progress

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class ModpackRepositoryImpl(
    private val appPaths: AppPaths,
    private val fileValidationService: FileValidationService,
    private val modpackParsers: Map<String, ModpackManifestParser>,
) : ModpackRepository {
    private fun modpackZipPath(platformId: String, projectId: String, fileId: String): Path =
        Path(appPaths.downloadsTmp, "modpack-$platformId-$projectId-$fileId.zip")

    context(_: Raise<ModpackResolutionError>)
    override suspend fun getModpackManifest(
        projectId: ProjectId,
        fileId: String,
        onProgress: (Progress) -> Unit
    ): ModpackManifest {
        val zipPath = modpackZipPath(projectId.platform, projectId.id, fileId)
        val request = projectFileDownloadRequest(projectId.platform, projectId.id, fileId, zipPath)
        recover({
            fileValidationService.submit(listOf(request), onProgress)
        }) { _: NonEmptyList<FileValidationFailure> ->
            raise(ModpackResolutionError.DownloadFailed)
        }
        val parser = ensureNotNull(modpackParsers[projectId.platform]) { ModpackResolutionError.UnsupportedPlatform }
        return parser.parse(zipPath, projectId, fileId)
    }

    context(_: Raise<ModpackResolutionError>)
    override suspend fun resolveModpackManifestFiles(
        manifest: ModpackManifest,
        onProgress: (Progress) -> Unit
    ): ModpackManifest {
        val zipPath = modpackZipPath(manifest.platformId, manifest.projectId, manifest.fileId)
        val parser = ensureNotNull(modpackParsers[manifest.platformId]) { ModpackResolutionError.UnsupportedPlatform }
        return parser.resolveFiles(zipPath, manifest, onProgress)
    }

    context(_: Raise<ModpackResolutionError>)
    override suspend fun parseLocalModpack(sourcePath: String, platformId: String): ModpackManifest {
        val zipPath = Path(sourcePath)
        val parser = ensureNotNull(modpackParsers[platformId]) { ModpackResolutionError.UnsupportedPlatform }
        val projectId = ProjectId(id = "local", platform = platformId)
        val manifest = parser.parse(zipPath, projectId, fileId = "local")
        return parser.resolveFiles(zipPath, manifest)
    }

    context(_: Raise<ModpackInstallError>)
    override suspend fun installModpackFiles(
        manifest: ModpackManifest,
        instanceId: String,
        localSourcePath: String?,
        preserveUserState: Boolean,
        onProgress: (Progress) -> Unit
    ): ModpackInstallResult {
        val gameDir = appPaths.gameDir(instanceId)
        val autoFiles = manifest.files.filter { it.manualDownloadUrl == null }
        val requests = autoFiles.map { file ->
            val targetPath = Path(gameDir, *file.targetPath.pathSegments())
            val cacheKind = file.kind.toContentCacheKind()
            val downloadUrl = file.downloadUrl
            if (downloadUrl != null) {
                FileValidationRequest.Url(
                    downloadUrl,
                    targetPath,
                    file.size,
                    file.sha1,
                    file.md5,
                    cacheKind = cacheKind
                )
            } else {
                val providerProjectId = ensureNotNull(file.providerProjectId) { ModpackInstallError.InvalidFileEntry }
                val providerFileId = ensureNotNull(file.providerFileId) { ModpackInstallError.InvalidFileEntry }
                projectFileDownloadRequest(
                    file.platformId ?: manifest.platformId,
                    providerProjectId,
                    providerFileId,
                    targetPath,
                    file.size,
                    file.sha1,
                    file.md5,
                    cacheKind
                )
            }
        }
        recover({
            fileValidationService.submit(requests, onProgress)
        }) { _: NonEmptyList<FileValidationFailure> ->
            raise(ModpackInstallError.DownloadFailed)
        }

        val zipPath =
            localSourcePath?.let(::Path) ?: modpackZipPath(manifest.platformId, manifest.projectId, manifest.fileId)
        val overridePaths = try {
            withContext(Dispatchers.IO) {
                extractOverrides(zipPath, gameDir, manifest.overridesDirs, preserveUserState)
            }
        } catch (_: Exception) {
            currentCoroutineContext().ensureActive()
            raise(ModpackInstallError.OverridesExtractionFailed)
        } finally {
            // Never delete a caller-owned local file, only our own downloaded tmp copy
            if (localSourcePath == null && zipPath.exists()) zipPath.deleteRecursively()
        }

        val installedFiles = autoFiles
            .filter { it.kind != ModpackFileKind.Other }
            .map { file ->
                InstalledModpackFile(
                    fileName = file.targetPath.substringAfterLast('/'),
                    kind = file.kind,
                    providerProjectId = file.providerProjectId,
                    providerFileId = file.providerFileId,
                    platformId = file.platformId
                )
            }
        return ModpackInstallResult(installedFiles, overridePaths)
    }

    private fun ModpackFileKind.toContentCacheKind(): ContentCacheKind? = when (this) {
        ModpackFileKind.Mod -> ContentCacheKind.Mod
        ModpackFileKind.ResourcePack -> ContentCacheKind.ResourcePack
        ModpackFileKind.ShaderPack -> ContentCacheKind.ShaderPack
        ModpackFileKind.Other -> null
    }

    private fun extractOverrides(
        zipPath: Path,
        gameDir: Path,
        overridesDirs: List<String>,
        preserveUserState: Boolean
    ): List<String> {
        val extractedPaths = mutableListOf<String>()
        Zip.open(zipPath).use { zip ->
            zip.forEachEntry { entry ->
                if (entry.isDirectory) return@forEachEntry
                val entryPath = entry.path.toZipEntryName()
                val matchingDir = overridesDirs.firstOrNull { entryPath.startsWith("$it/") } ?: return@forEachEntry
                val relativePath = entryPath.removePrefix("$matchingDir/").pathSegments().joinToString("/")
                val target = Path(gameDir, *relativePath.pathSegments())
                val userState = isUserStatePath(relativePath)
                if (!(preserveUserState && userState && SystemFileSystem.exists(target))) {
                    target.parent?.let { SystemFileSystem.createDirectories(it) }
                    entry.readToPath(target)
                }
                if (!userState) extractedPaths += relativePath
            }
        }
        return extractedPaths
    }
}

private fun String.pathSegments(): Array<String> = split('/').filter { it.isNotBlank() }.toTypedArray()
