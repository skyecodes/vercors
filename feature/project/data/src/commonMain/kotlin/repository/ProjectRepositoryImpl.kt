package app.vercors.feature.project.data.repository

import app.vercors.core.domain.Page
import app.vercors.core.filesystem.createParentDirectories
import app.vercors.core.filesystem.deleteRecursively
import app.vercors.core.filesystem.exists
import app.vercors.core.filesystem.moveTo
import app.vercors.core.network.MetaApiConnectivity
import app.vercors.core.network.RemoteError
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.project.api.PlatformFiles
import app.vercors.feature.project.api.Platforms
import app.vercors.feature.project.api.ProjectFiles
import app.vercors.feature.project.api.Projects
import app.vercors.feature.project.api.dto.ProjectLoaderData
import app.vercors.feature.project.data.*
import app.vercors.feature.project.domain.model.*
import app.vercors.feature.project.domain.repository.ProjectRepository
import app.vercors.shared.api.ApiV1
import app.vercors.shared.data.AppPaths
import app.vercors.shared.data.FileValidationFailure
import app.vercors.shared.data.FileValidationService
import app.vercors.shared.data.extractWorldZip
import arrow.core.NonEmptyList
import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import arrow.core.raise.context.withError
import arrow.core.raise.recover
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.ktor.client.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.io.files.Path
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

private const val HASH_LOOKUP_CHUNK_SIZE = 500

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class ProjectRepositoryImpl(
    private val httpClient: HttpClient,
    private val metaApiConnectivity: MetaApiConnectivity,
    private val json: Json,
    private val appPaths: AppPaths,
    private val fileValidationService: FileValidationService,
) : ProjectRepository {
    context(_: Raise<PlatformError>)
    override suspend fun getPlatformList(): List<ProjectPlatform> =
        metaRequest {
            httpClient.request(
                endpoint = ApiV1 / Platforms / Platforms.list,
                configure = { expectSuccess = false }
            )
        }.handle(
            handle1 = { raise(it.toDomain()) },
            transform = { list -> list.map { it.toDomain() } }
        )

    context(_: Raise<PlatformNotFound>, _: Raise<PlatformError>)
    override suspend fun getFeaturedProjectList(
        platformId: PlatformId
    ): FeaturedProjectList =
        metaRequest {
            httpClient.request(
                endpoint = ApiV1 / Platforms / Platforms.PlatformId(platformId) / Platforms.PlatformId.featuredProjects,
                configure = { expectSuccess = false }
            )
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
    ): Page<BriefProject> = metaRequest {
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
    }.handle(
        handle1 = { raise(it.toDomain()) },
        handle2 = { raise(it.toDomain()) },
        handle3 = { raise(if (it.name == "type") UnsupportedProjectType(type) else it.toDomain()) },
        transform = { data -> Page(items = data.results.map { it.toDomain() }, total = data.total) }
    )

    context(_: Raise<ProjectDetailsError>)
    override suspend fun getProject(
        projectId: ProjectId
    ): Project = metaRequest {
        httpClient.request(
            endpoint = projectResource(projectId) / Projects.ProjectId.get,
            configure = { expectSuccess = false }
        )
    }.handle(
        handle1 = { raise(it.toDomain()) },
        handle2 = { raise(it.toDomain()) },
        transform = { it.toDomain() }
    )

    context(_: Raise<ProjectDetailsError>)
    override suspend fun getProjectFiles(
        projectId: ProjectId,
        gameVersion: String?,
        loader: ModLoaderType?
    ): List<ProjectFile> = metaRequest {
        httpClient.request(
            endpoint = projectResource(projectId) / ProjectFiles / ProjectFiles.list,
            parameters = {
                this.gameVersion = gameVersion
                this.loader = loader?.let { json.encodeToJsonElement(it.toMetaData()).jsonPrimitive.content }
            },
            configure = { expectSuccess = false }
        )
    }.handle(
        handle1 = { raise(it.toDomain()) },
        handle2 = { raise(it.toDomain()) },
        handle3 = { raise(it.toDomain()) },
        transform = { list -> list.map { it.toDomain(projectId.platform) } }
    )

    context(_: Raise<ProjectFileDownloadError>)
    override suspend fun downloadProjectFiles(
        candidates: List<ProjectFileDownloadCandidate>,
        instanceId: String,
        onProgress: (Progress) -> Unit
    ) {
        val downloads = candidates.map { candidate ->
            val finalPath = Path(appPaths.contentDir(candidate.type, instanceId), candidate.fileName)
            val tempPath = Path(appPaths.downloadsTmp, "${Uuid.random()}-${candidate.fileName}")
            val request = projectFileDownloadRequest(
                platformId = candidate.projectId.platform,
                projectId = candidate.projectId.id,
                fileId = candidate.fileId,
                path = tempPath,
                size = candidate.size,
                sha1 = candidate.sha1,
                md5 = candidate.md5,
                cacheKind = candidate.type.toContentCacheKind()
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
                // atomicMove overwrites finalPath; deleting first would reopen the race it closes
                tempPath.moveTo(finalPath)
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                raise(ProjectFileDownloadFailed)
            }
        }
    }

    context(_: Raise<ProjectDetailsError>)
    override suspend fun getProjectFile(projectId: ProjectId, fileId: String): ProjectFile =
        metaRequest {
            httpClient.request(
                endpoint = projectResource(projectId) / ProjectFiles / ProjectFiles.FileId(fileId) / ProjectFiles.FileId.get,
                configure = { expectSuccess = false }
            )
        }.handle(
            handle1 = { raise(it.toDomain()) },
        handle2 = { raise(it.toDomain()) },
        transform = { it.toDomain(projectId.platform) }
    )

    context(_: Raise<ProjectDetailsError>)
    override suspend fun getProjectFilesByIds(platformId: PlatformId, fileIds: List<String>): List<ProjectFile> =
        metaRequest {
            httpClient.request(
                endpoint = ApiV1 / Platforms / Platforms.PlatformId(platformId) / PlatformFiles / PlatformFiles.list,
                parameters = { this.fileIds = fileIds },
                configure = { expectSuccess = false }
            )
        }.handle(
            handle1 = { raise(it.toDomain()) },
            handle2 = { raise(it.toDomain()) },
            transform = { list -> list.map { it.toDomain(platformId) } }
        )

    context(_: Raise<ProjectDetailsError>)
    override suspend fun findFilesByHashes(platformId: PlatformId, hashes: List<FileHashes>): List<ProjectFileMatch> =
        hashes.chunked(HASH_LOOKUP_CHUNK_SIZE).flatMap { chunk ->
            metaRequest {
                httpClient.request(
                    endpoint = ApiV1 / Platforms / Platforms.PlatformId(platformId) / PlatformFiles / PlatformFiles.lookup,
                    input = chunk.map { it.toData() },
                    configure = { expectSuccess = false }
                )
            }.handle(
                handle1 = { raise(it.toDomain()) },
                handle2 = { raise(it.toDomain()) },
                transform = { list -> list.map { it.toDomain(platformId) } }
            )
        }

    context(_: Raise<PlatformError>)
    private suspend fun <T> metaRequest(call: suspend () -> T): T =
        withError(RemoteError::toPlatformError) { metaApiConnectivity.request(call) }

    private fun projectResource(projectId: ProjectId) =
        ApiV1 / Platforms / Platforms.PlatformId(projectId.platform) / Projects / Projects.ProjectId(projectId.id)

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
        } catch (_: Exception) {
            currentCoroutineContext().ensureActive()
            raise(ProjectFileDownloadFailed)
        } finally {
            if (zipPath.exists()) zipPath.deleteRecursively()
        }
    }
}

private fun ModLoaderType.toMetaData() = when (this) {
    ModLoaderType.Forge -> ProjectLoaderData.Forge
    ModLoaderType.Quilt -> ProjectLoaderData.Quilt
    ModLoaderType.Fabric -> ProjectLoaderData.Fabric
    ModLoaderType.NeoForge -> ProjectLoaderData.NeoForge
}
