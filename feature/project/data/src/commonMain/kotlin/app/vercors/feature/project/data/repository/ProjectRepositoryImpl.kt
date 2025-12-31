package app.vercors.feature.project.data.repository

import app.vercors.core.domain.Page
import app.vercors.core.domain.Result
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.project.data.toData
import app.vercors.feature.project.data.toDomain
import app.vercors.feature.project.domain.model.*
import app.vercors.feature.project.domain.repository.ProjectRepository
import app.vercors.meta.client.project.ProjectApi
import app.vercors.meta.lib.project.ProjectLoaderData
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.koin.core.annotation.Single

@Single
internal class ProjectRepositoryImpl(
    private val projectApi: ProjectApi,
    private val json: Json
) : ProjectRepository {
    override suspend fun getPlatformList(): Result<List<ProjectPlatform>> =
        projectApi.getPlatformList().map { it.map { it.toDomain() } }.toResult()

    override suspend fun getFeaturedProjectList(
        platformId: PlatformId
    ): Result<Map<ProjectType, List<BriefProject>>> = projectApi.getFeaturedProjectList(platformId).map { res ->
        res.entries.associate { (key, value) ->
            key.toDomain() to value.map { it.toDomain() }
        }
    }.toResult()

    override suspend fun getProjectList(
        platformId: PlatformId,
        type: ProjectType,
        loaders: List<ModLoaderType>?,
        gameVersions: List<String>?,
        query: String?,
        categories: List<String>?,
        sortBy: ProjectSortBy?,
        pageSize: Int?,
        offset: Int?
    ): Result<Page<BriefProject>> = projectApi.getProjectList(
        platformId = platformId,
        projectType = json.encodeToJsonElement(type.toData()).jsonPrimitive.content,
        loaders = loaders?.let { loaders -> json.encodeToJsonElement(loaders.map { it.toMetaData() }).jsonArray.map { it.jsonPrimitive.content } },
        gameVersions = gameVersions,
        categories = categories,
        query = query,
        sortBy = sortBy?.let { json.encodeToJsonElement(it.toData()).jsonPrimitive.content },
        pageSize = pageSize,
        offset = offset
    ).map { list -> Page(items = list.results.map { it.toDomain() }, total = list.total) }.toResult()

    override suspend fun getProject(
        projectId: ProjectId
    ): Result<Project> = projectApi.getProject(projectId.platform, projectId.id).map { it.toDomain() }.toResult()
}

private fun ModLoaderType.toMetaData() = when (this) {
    ModLoaderType.Forge -> ProjectLoaderData.Forge
    ModLoaderType.Quilt -> ProjectLoaderData.Quilt
    ModLoaderType.Fabric -> ProjectLoaderData.Fabric
    ModLoaderType.NeoForge -> ProjectLoaderData.NeoForge
}
