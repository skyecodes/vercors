package app.vercors.feature.project.data.repository

import app.vercors.core.domain.Page
import app.vercors.core.filesystem.*
import app.vercors.core.system.SystemService
import app.vercors.feature.game.domain.model.ModLoaderType
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
import app.vercors.feature.project.domain.model.*
import app.vercors.feature.project.domain.model.modpack.*
import app.vercors.feature.project.domain.repository.ProjectRepository
import app.vercors.shared.api.ApiV1
import app.vercors.shared.data.AppPaths
import app.vercors.shared.data.FileValidationFailure
import app.vercors.shared.data.FileValidationRequest
import app.vercors.shared.data.FileValidationService
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
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
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
import kotlin.uuid.Uuid

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
        httpClient.request(
            endpoint = ApiV1 / Platforms / Platforms.list
        ).handle(
            handle1 = { raise(it.toDomain()) },
            transform = { list -> list.map { it.toDomain() } }
        )

    context(_: Raise<PlatformNotFound>, _: Raise<PlatformError>)
    override suspend fun getFeaturedProjectList(
        platformId: PlatformId
    ): FeaturedProjectList =
        httpClient.request(
            endpoint = ApiV1 / Platforms / Platforms.PlatformId(platformId) / Platforms.PlatformId.featuredProjects
        ).handle(
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
    ): Page<BriefProject> = httpClient.request(
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
        }
    ).handle(
        handle1 = { raise(it.toDomain()) },
        handle2 = { raise(it.toDomain()) },
        handle3 = { raise(it.toDomain()) },
        transform = { data -> Page(items = data.results.map { it.toDomain() }, total = data.total) }
    )

    context(_: Raise<ProjectDetailsError>)
    override suspend fun getProject(
        projectId: ProjectId
    ): Project = httpClient.request(
        endpoint = ApiV1 / Platforms / Platforms.PlatformId(projectId.platform) / Projects / Projects.ProjectId(
            projectId.id
        ) / Projects.ProjectId.get
    ).handle(
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
    ): List<ProjectFile> = httpClient.request(
        endpoint = ApiV1 / Platforms / Platforms.PlatformId(projectId.platform) / Projects / Projects.ProjectId(
            projectId.id
        ) / ProjectFiles / ProjectFiles.list,
        parameters = {
            this.gameVersion = gameVersion
            this.loader = loader?.let { json.encodeToJsonElement(it.toMetaData()).jsonPrimitive.content }
        }
    ).handle(
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
    override suspend fun downloadProjectFile(
        projectId: ProjectId,
        fileId: String,
        fileName: String,
        instanceId: String,
        type: ProjectType
    ) {
        val finalPath = Path(type.targetDir(instanceId), fileName)
        val tempPath = Path(appPaths.downloadsTmp, "${Uuid.random()}-$fileName")
        val request = projectFileDownloadRequest(
            platformId = projectId.platform,
            projectId = projectId.id,
            fileId = fileId,
            path = tempPath
        )
        recover({
            fileValidationService.submit(listOf(request)) {}
        }) { _: NonEmptyList<FileValidationFailure> ->
            raise(ProjectFileDownloadFailed)
        }
        try {
            if (finalPath.exists()) finalPath.deleteRecursively()
            finalPath.createParentDirectories()
            tempPath.moveTo(finalPath)
        } catch (_: Exception) {
            currentCoroutineContext().ensureActive()
            raise(ProjectFileDownloadFailed)
        }
    }

    private fun ProjectType.targetDir(instanceId: String): Path = when (this) {
        ProjectType.Mod, ProjectType.Modpack -> appPaths.modsDir(instanceId)
        ProjectType.ResourcePack -> appPaths.resourcePacksDir(instanceId)
        ProjectType.ShaderPack -> appPaths.shaderPacksDir(instanceId)
        ProjectType.DataPack, ProjectType.World -> error("Install is not supported for project type $this")
    }

    context(_: Raise<ProjectDetailsError>)
    override suspend fun getProjectFile(projectId: ProjectId, fileId: String): ProjectFile = httpClient.request(
        endpoint = ApiV1 / Platforms / Platforms.PlatformId(projectId.platform) / Projects / Projects.ProjectId(
            projectId.id
        ) / ProjectFiles / ProjectFiles.ProjectFileId(fileId) / ProjectFiles.ProjectFileId.get
    ).handle(
        handle1 = {
            when (it) {
                is ProjectNotFoundData -> raise(it.toDomain())
                is PlatformNotFoundData -> raise(it.toDomain())
            }
        },
        handle2 = { raise(it.toDomain()) },
        transform = { it.toDomain(projectId.platform) }
    )

    private fun modpackZipPath(platformId: String, projectId: String, fileId: String): Path =
        Path(appPaths.downloadsTmp, "modpack-$platformId-$projectId-$fileId.zip")

    context(_: Raise<ModpackResolutionError>)
    override suspend fun getModpackManifest(projectId: ProjectId, fileId: String): ModpackManifest {
        val zipPath = modpackZipPath(projectId.platform, projectId.id, fileId)
        val request = projectFileDownloadRequest(projectId.platform, projectId.id, fileId, zipPath)
        recover({
            fileValidationService.submit(listOf(request)) {}
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

    context(_: Raise<ModpackInstallError>)
    override suspend fun installModpackFiles(
        manifest: ModpackManifest,
        instanceId: String,
        onProgress: (Progress) -> Unit
    ): List<InstalledModpackFile> {
        val gameDir = appPaths.gameDir(instanceId)
        val autoFiles = manifest.files.filter { it.manualDownloadUrl == null }
        val requests = autoFiles.map { file ->
            val targetPath = Path(gameDir, *file.targetPath.pathSegments())
            val downloadUrl = file.downloadUrl
            if (downloadUrl != null) {
                FileValidationRequest.Url(downloadUrl, targetPath, file.size, file.sha1)
            } else {
                projectFileDownloadRequest(
                    manifest.platformId,
                    requireNotNull(file.providerProjectId),
                    requireNotNull(file.providerFileId),
                    targetPath,
                    file.size,
                    file.sha1
                )
            }
        }
        recover({
            fileValidationService.submit(requests, onProgress)
        }) { _: NonEmptyList<FileValidationFailure> ->
            raise(ModpackInstallDownloadFailed)
        }

        val zipPath = modpackZipPath(manifest.platformId, manifest.projectId, manifest.fileId)
        try {
            extractOverrides(zipPath, gameDir, manifest.overridesDirs)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            raise(ModpackOverridesExtractionFailed)
        } finally {
            if (zipPath.exists()) zipPath.deleteRecursively()
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
    override suspend fun awaitManualDownload(fileName: String, instanceId: String, type: ProjectType) {
        val downloadsDir = Path(ensureNotNull(systemService.downloadsDir()) { ManualDownloadFailed })
        val sourcePath = Path(downloadsDir, fileName)
        if (!sourcePath.exists()) {
            fileWatcherService.watchDirectory(downloadsDir)
                .first { it.type == FileEvent.Type.CREATE && it.path.name == fileName }
        }
        val targetPath = Path(type.targetDir(instanceId), fileName)
        try {
            sourcePath.copyTo(targetPath)
            sourcePath.delete()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            raise(ManualDownloadFailed)
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
