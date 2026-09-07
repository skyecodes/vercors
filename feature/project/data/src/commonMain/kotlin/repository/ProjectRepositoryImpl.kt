package app.vercors.feature.project.data.repository

import app.vercors.core.domain.Page
import app.vercors.core.filesystem.*
import app.vercors.core.network.RemoteError
import app.vercors.core.network.makeMetaApiRequest
import app.vercors.core.system.SystemService
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.project.api.PlatformFiles
import app.vercors.feature.project.api.Platforms
import app.vercors.feature.project.api.ProjectFiles
import app.vercors.feature.project.api.Projects
import app.vercors.feature.project.api.dto.PlatformNotFoundData
import app.vercors.feature.project.api.dto.ProjectLoaderData
import app.vercors.feature.project.api.dto.ProjectNotFoundData
import app.vercors.feature.project.data.modpack.ModpackManifestParser
import app.vercors.feature.project.data.projectFileDownloadRequest
import app.vercors.feature.project.data.toData
import app.vercors.feature.project.data.toDomain
import app.vercors.feature.project.data.toPlatformError
import app.vercors.feature.project.domain.model.*
import app.vercors.feature.project.domain.model.modpack.*
import app.vercors.feature.project.domain.repository.ProjectRepository
import app.vercors.shared.api.ApiV1
import app.vercors.shared.data.*
import arrow.core.NonEmptyList
import arrow.core.raise.Raise
import arrow.core.raise.context.ensureNotNull
import arrow.core.raise.context.raise
import arrow.core.raise.context.withError
import arrow.core.raise.recover
import de.jonasbroeckmann.kzip.Zip
import de.jonasbroeckmann.kzip.forEachEntry
import de.jonasbroeckmann.kzip.open
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import opensavvy.progress.Progress
import opensavvy.spine.api.div
import opensavvy.spine.api.invoke
import opensavvy.spine.client.handle
import opensavvy.spine.client.request
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.Uuid

/** Polling interval used to detect when a manually downloaded file has stopped growing. */
private val STABLE_POLL_INTERVAL = 500.milliseconds

/** Number of consecutive unchanged size polls required before treating a manual download as finished. */
private const val STABLE_POLL_THRESHOLD = 3

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class ProjectRepositoryImpl(
    private val httpClient: HttpClient,
    private val json: Json,
    private val appPaths: AppPaths,
    private val fileValidationService: FileValidationService,
    private val modpackParsers: Map<String, ModpackManifestParser>,
    private val systemService: SystemService,
    private val fileWatcherService: FileWatcherService
) : ProjectRepository {

    context(_: Raise<PlatformError>)
    override suspend fun getPlatformList(): List<ProjectPlatform> =
        withError(RemoteError::toPlatformError) {
            makeMetaApiRequest {
                httpClient.request(
                    endpoint = ApiV1 / Platforms / Platforms.list,
                    configure = { expectSuccess = false }
                )
            }
        }.handle(
            handle1 = { raise(it.toDomain()) },
            transform = { list -> list.map { it.toDomain() } }
        )

    context(_: Raise<PlatformNotFound>, _: Raise<PlatformError>)
    override suspend fun getFeaturedProjectList(
        platformId: PlatformId
    ): FeaturedProjectList =
        withError<PlatformError, RemoteError, _>(RemoteError::toPlatformError) {
            makeMetaApiRequest {
                httpClient.request(
                    endpoint = ApiV1 / Platforms / Platforms.PlatformId(platformId) / Platforms.PlatformId.featuredProjects,
                    configure = { expectSuccess = false }
                )
            }
        }.handle(
            handle1 = { raise(it.toDomain()) },
            handle2 = { raise(it.toDomain()) },
            transform = { map ->
                map.entries.associate { (key, value) ->
                    key.toDomain() to value.map { it.toDomain() }
                }
            }
        )

    context(_: Raise<ProjectSearchError>)
    override suspend fun searchProjects(
        platformId: PlatformId,
        type: ProjectType,
        loaders: List<ModLoaderType>?,
        gameVersions: List<String>?,
        query: String?,
        categories: List<String>?,
        sortBy: ProjectSortBy?,
        pageSize: Int?,
        offset: Int?
    ): Page<BriefProject> = withError(RemoteError::toPlatformError) {
        makeMetaApiRequest {
            httpClient.request(
                endpoint = ApiV1 / Platforms / Platforms.PlatformId(platformId) / Projects / Projects.search,
                parameters = {
                    this.type = json.encodeToJsonElement(type.toData()).jsonPrimitive.content
                    loaders?.let { loaders ->
                        this.loaders =
                            json.encodeToJsonElement(loaders.map { it.toMetaData() }).jsonArray.map { it.jsonPrimitive.content }
                    }
                    gameVersions?.let { this.gameVersions = it }
                    categories?.let { this.categories = it }
                    this.query = query
                    this.sortBy = sortBy?.let { json.encodeToJsonElement(it.toData()).jsonPrimitive.content }
                    this.pageSize = pageSize
                    this.offset = offset
                },
                configure = { expectSuccess = false }
            )
        }
    }.handle(
        handle1 = { raise(it.toDomain()) },
        handle2 = { raise(it.toDomain()) },
        handle3 = { raise(it.toDomain()) },
        transform = { data -> Page(items = data.results.map { it.toDomain() }, total = data.total) }
    )

    context(_: Raise<ProjectDetailsError>)
    override suspend fun getProject(
        projectId: ProjectId
    ): Project = withError(RemoteError::toPlatformError) {
        makeMetaApiRequest {
            httpClient.request(
                endpoint = ApiV1 / Platforms / Platforms.PlatformId(projectId.platform) / Projects / Projects.ProjectId(
                    projectId.id
                ) / Projects.ProjectId.get,
                configure = { expectSuccess = false }
            )
        }
    }.handle(
        handle1 = {
            when (it) {
                is ProjectNotFoundData -> raise(it.toDomain())
                is PlatformNotFoundData -> raise(it.toDomain())
            }
        },
        handle2 = { raise(it.toDomain()) },
        transform = { it.toDomain() }
    )

    context(_: Raise<ProjectDetailsError>)
    override suspend fun getProjectFiles(
        projectId: ProjectId,
        gameVersion: String?,
        loader: ModLoaderType?
    ): List<ProjectFile> = withError(RemoteError::toPlatformError) {
        makeMetaApiRequest {
            httpClient.request(
                endpoint = ApiV1 / Platforms / Platforms.PlatformId(projectId.platform) / Projects / Projects.ProjectId(
                    projectId.id
                ) / ProjectFiles / ProjectFiles.list,
                parameters = {
                    this.gameVersion = gameVersion
                    this.loader = loader?.let { json.encodeToJsonElement(it.toMetaData()).jsonPrimitive.content }
                },
                configure = { expectSuccess = false }
            )
        }
    }.handle(
        handle1 = {
            when (it) {
                is ProjectNotFoundData -> raise(it.toDomain())
                is PlatformNotFoundData -> raise(it.toDomain())
            }
        },
        handle2 = { raise(it.toDomain()) },
        transform = { list -> list.map { it.toDomain(projectId.platform) } }
    )

    context(_: Raise<ProjectFileDownloadError>)
    override suspend fun downloadProjectFiles(
        candidates: List<ProjectFileDownloadCandidate>,
        instanceId: String,
        type: ProjectType,
        onProgress: (Progress) -> Unit
    ) {
        val targetDir = type.targetDir(instanceId)
        val cacheKind = type.toContentCacheKind()
        val downloads = candidates.map { candidate ->
            val finalPath = Path(targetDir, candidate.fileName)
            val tempPath = Path(appPaths.downloadsTmp, "${Uuid.random()}-${candidate.fileName}")
            val request = projectFileDownloadRequest(
                platformId = candidate.projectId.platform,
                projectId = candidate.projectId.id,
                fileId = candidate.fileId,
                path = tempPath,
                size = candidate.size,
                sha1 = candidate.sha1,
                md5 = candidate.md5,
                cacheKind = cacheKind
            )
            Triple(request, tempPath, finalPath)
        }
        recover({
            fileValidationService.submit(downloads.map { it.first }, onProgress)
        }) { _: NonEmptyList<FileValidationFailure> ->
            raise(ProjectFileDownloadFailed)
        }
        downloads.forEach { (_, tempPath, finalPath) ->
            try {
                finalPath.createParentDirectories()
                // atomicMove overwrites finalPath if it already exists — no need to delete it first, and doing so
                // would just reopen the window this atomic move is meant to close.
                tempPath.moveTo(finalPath)
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                raise(ProjectFileDownloadFailed)
            }
        }
    }

    private fun ProjectType.targetDir(instanceId: String): Path = when (this) {
        ProjectType.Mod, ProjectType.Modpack -> appPaths.modsDir(instanceId)
        ProjectType.ResourcePack -> appPaths.resourcePacksDir(instanceId)
        ProjectType.ShaderPack -> appPaths.shaderPacksDir(instanceId)
        ProjectType.DataPack, ProjectType.World -> error("Install is not supported for project type $this")
    }

    private fun ProjectType.toContentCacheKind(): ContentCacheKind? = when (this) {
        ProjectType.Mod, ProjectType.Modpack -> ContentCacheKind.Mod
        ProjectType.ResourcePack -> ContentCacheKind.ResourcePack
        ProjectType.ShaderPack -> ContentCacheKind.ShaderPack
        ProjectType.DataPack, ProjectType.World -> null
    }

    private fun ModpackFileKind.toContentCacheKind(): ContentCacheKind? = when (this) {
        ModpackFileKind.Mod -> ContentCacheKind.Mod
        ModpackFileKind.ResourcePack -> ContentCacheKind.ResourcePack
        ModpackFileKind.ShaderPack -> ContentCacheKind.ShaderPack
        ModpackFileKind.Other -> null
    }

    context(_: Raise<ProjectDetailsError>)
    override suspend fun getProjectFile(projectId: ProjectId, fileId: String): ProjectFile =
        withError(RemoteError::toPlatformError) {
            makeMetaApiRequest {
                httpClient.request(
                    endpoint = ApiV1 / Platforms / Platforms.PlatformId(projectId.platform) / Projects / Projects.ProjectId(
                        projectId.id
                    ) / ProjectFiles / ProjectFiles.ProjectFileId(fileId) / ProjectFiles.ProjectFileId.get,
                    configure = { expectSuccess = false }
                )
            }
        }.handle(
        handle1 = {
            when (it) {
                is ProjectNotFoundData -> raise(it.toDomain())
                is PlatformNotFoundData -> raise(it.toDomain())
            }
        },
        handle2 = { raise(it.toDomain()) },
        transform = { it.toDomain(projectId.platform) }
    )

    context(_: Raise<ProjectDetailsError>)
    override suspend fun getProjectFilesByIds(platformId: PlatformId, fileIds: List<String>): List<ProjectFile> =
        withError(RemoteError::toPlatformError) {
            makeMetaApiRequest {
                httpClient.request(
                    endpoint = ApiV1 / Platforms / Platforms.PlatformId(platformId) / PlatformFiles / PlatformFiles.get,
                    parameters = { this.fileIds = fileIds },
                    configure = { expectSuccess = false }
                )
            }
        }.handle(
            handle1 = { raise(it.toDomain()) },
            handle2 = { raise(it.toDomain()) },
            transform = { list -> list.map { it.toDomain(platformId) } }
        )

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
            raise(ModpackDownloadFailed)
        }
        val parser = ensureNotNull(modpackParsers[projectId.platform]) { ModpackUnsupportedPlatform }
        return parser.parse(zipPath, projectId, fileId)
    }

    context(_: Raise<ModpackResolutionError>)
    override suspend fun resolveModpackManifestFiles(
        manifest: ModpackManifest,
        onProgress: (Progress) -> Unit
    ): ModpackManifest {
        val zipPath = modpackZipPath(manifest.platformId, manifest.projectId, manifest.fileId)
        val parser = ensureNotNull(modpackParsers[manifest.platformId]) { ModpackUnsupportedPlatform }
        return parser.resolveFiles(zipPath, manifest, onProgress)
    }

    context(_: Raise<ModpackResolutionError>)
    override suspend fun parseLocalModpack(sourcePath: String, platformId: String): ModpackManifest {
        val zipPath = Path(sourcePath)
        val parser = ensureNotNull(modpackParsers[platformId]) { ModpackUnsupportedPlatform }
        val projectId = ProjectId(id = "local", platform = platformId)
        val manifest = parser.parse(zipPath, projectId, fileId = "local")
        return parser.resolveFiles(zipPath, manifest)
    }

    context(_: Raise<ModpackInstallError>)
    override suspend fun installModpackFiles(
        manifest: ModpackManifest,
        instanceId: String,
        localSourcePath: String?,
        onProgress: (Progress) -> Unit
    ): List<InstalledModpackFile> {
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
                val providerProjectId = ensureNotNull(file.providerProjectId) { ModpackInvalidFileEntry }
                val providerFileId = ensureNotNull(file.providerFileId) { ModpackInvalidFileEntry }
                projectFileDownloadRequest(
                    manifest.platformId,
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
            raise(ModpackInstallDownloadFailed)
        }

        val zipPath =
            localSourcePath?.let(::Path) ?: modpackZipPath(manifest.platformId, manifest.projectId, manifest.fileId)
        try {
            extractOverrides(zipPath, gameDir, manifest.overridesDirs)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            raise(ModpackOverridesExtractionFailed)
        } finally {
            // Never delete a caller-owned local file (localSourcePath != null) — only vercors' own downloaded tmp copy.
            if (localSourcePath == null && zipPath.exists()) zipPath.deleteRecursively()
        }

        return autoFiles
            .filter { it.kind != ModpackFileKind.Other }
            .map { file ->
                InstalledModpackFile(
                    fileName = file.targetPath.substringAfterLast('/'),
                    kind = file.kind,
                    providerProjectId = file.providerProjectId,
                    providerFileId = file.providerFileId
                )
            }
    }

    context(_: Raise<ManualDownloadError>)
    override suspend fun awaitManualDownload(
        fileName: String,
        instanceId: String,
        type: ProjectType,
        size: Long?,
        sha1: String?,
        md5: String?
    ) {
        val sourcePath = awaitStableDownloadInDownloadsFolder(fileName, size)
        val targetPath = Path(type.targetDir(instanceId), fileName)
        try {
            sourcePath.copyTo(targetPath)
            sourcePath.delete()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            raise(ManualDownloadFailed)
        }
        recover({
            with(fileValidationService) {
                val request = FileValidationRequest.Url(
                    url = "",
                    path = targetPath,
                    size = size,
                    sha1 = sha1,
                    md5 = md5,
                    cacheKind = type.toContentCacheKind()
                )
                request.validate()
                request.populateCache()
            }
        }) { _: FileValidationFailure ->
            if (targetPath.exists()) targetPath.deleteRecursively()
            raise(ManualDownloadIntegrityMismatch)
        }
    }

    /**
     * Suspends until [fileName] appears in the OS Downloads folder (or immediately if it's already there) and
     * stops growing — shared preamble for [awaitManualDownload] and [awaitManualWorldDownload], which only differ
     * in how the stabilized file is materialized afterward (move into a flat directory vs. extract as a world).
     */
    context(_: Raise<ManualDownloadError>)
    private suspend fun awaitStableDownloadInDownloadsFolder(fileName: String, size: Long?): Path {
        val downloadsDir = Path(ensureNotNull(systemService.downloadsDir()) { ManualDownloadFailed })
        val sourcePath = Path(downloadsDir, fileName)
        if (!sourcePath.exists()) {
            fileWatcherService.watchDirectory(downloadsDir)
                .first { it.type == FileEvent.Type.CREATE && it.path.name == fileName }
        }
        // The file existing (or having just been created) doesn't mean the browser is done writing to it — wait
        // for it to stop growing before touching it, so we never move/validate a still-downloading file.
        awaitDownloadStable(sourcePath, size)
        return sourcePath
    }

    context(_: Raise<ProjectFileDownloadError>)
    override suspend fun installWorldFile(
        candidate: ProjectFileDownloadCandidate,
        instanceId: String,
        onProgress: (Progress) -> Unit
    ) {
        val zipPath = Path(appPaths.downloadsTmp, "${Uuid.random()}-${candidate.fileName}")
        val request = projectFileDownloadRequest(
            platformId = candidate.projectId.platform,
            projectId = candidate.projectId.id,
            fileId = candidate.fileId,
            path = zipPath,
            size = candidate.size,
            sha1 = candidate.sha1,
            md5 = candidate.md5
        )
        recover({
            fileValidationService.submit(listOf(request), onProgress)
        }) { _: NonEmptyList<FileValidationFailure> ->
            raise(ProjectFileDownloadFailed)
        }
        try {
            appPaths.extractWorldZip(zipPath, instanceId, candidate.fileName)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            raise(ProjectFileDownloadFailed)
        } finally {
            if (zipPath.exists()) zipPath.deleteRecursively()
        }
    }

    context(_: Raise<ManualDownloadError>)
    override suspend fun awaitManualWorldDownload(
        fileName: String,
        instanceId: String,
        size: Long?,
        sha1: String?,
        md5: String?
    ) {
        val sourcePath = awaitStableDownloadInDownloadsFolder(fileName, size)
        recover({
            with(fileValidationService) {
                FileValidationRequest.Url(url = "", path = sourcePath, size = size, sha1 = sha1, md5 = md5).validate()
            }
        }) { _: FileValidationFailure ->
            if (sourcePath.exists()) sourcePath.deleteRecursively()
            raise(ManualDownloadIntegrityMismatch)
        }
        try {
            appPaths.extractWorldZip(sourcePath, instanceId, fileName)
            sourcePath.delete()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            raise(ManualDownloadFailed)
        }
    }

    /**
     * Suspends until [path]'s size stops changing — either it reaches the already-known [expectedSize] (fast
     * path), or it stays unchanged across [STABLE_POLL_THRESHOLD] consecutive polls. There's no cross-platform
     * signal for "is another process still writing to this file" (`Path.lastModifiedOrNull()` is unavailable on
     * mingw), so polling size is the only reliable option here.
     */
    private suspend fun awaitDownloadStable(path: Path, expectedSize: Long?) {
        var lastSize: Long? = null
        var stableCount = 0
        while (true) {
            val currentSize = path.metadataOrNull()?.size
            when {
                currentSize != null && currentSize == expectedSize -> return
                currentSize != null && currentSize == lastSize -> {
                    stableCount++
                    if (stableCount >= STABLE_POLL_THRESHOLD) return
                }

                else -> {
                    lastSize = currentSize
                    stableCount = 0
                }
            }
            delay(STABLE_POLL_INTERVAL)
        }
    }

    private fun extractOverrides(zipPath: Path, gameDir: Path, overridesDirs: List<String>) {
        Zip.open(zipPath).use { zip ->
            zip.forEachEntry { entry ->
                if (entry.isDirectory) return@forEachEntry
                val entryPath = entry.path.toString()
                val matchingDir = overridesDirs.firstOrNull { entryPath.startsWith("$it/") } ?: return@forEachEntry
                val relativePath = entryPath.removePrefix("$matchingDir/")
                val target = Path(gameDir, *relativePath.pathSegments())
                target.parent?.let { SystemFileSystem.createDirectories(it) }
                entry.readToPath(target)
            }
        }
    }
}

private fun String.pathSegments(): Array<String> = split('/').filter { it.isNotBlank() }.toTypedArray()

private fun ModLoaderType.toMetaData() = when (this) {
    ModLoaderType.Forge -> ProjectLoaderData.Forge
    ModLoaderType.Quilt -> ProjectLoaderData.Quilt
    ModLoaderType.Fabric -> ProjectLoaderData.Fabric
    ModLoaderType.NeoForge -> ProjectLoaderData.NeoForge
}
