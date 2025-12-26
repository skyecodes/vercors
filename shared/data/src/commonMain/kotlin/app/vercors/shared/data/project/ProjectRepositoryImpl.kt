package app.vercors.shared.data.project

import app.vercors.meta.client.project.ProjectApi
import app.vercors.meta.lib.project.ProjectLoaderData
import app.vercors.shared.domain.Result
import app.vercors.shared.domain.loader.ModLoaderType
import app.vercors.shared.domain.project.ProjectRepository
import app.vercors.shared.domain.project.model.*
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
        pageSize: Int?
    ): Result<List<BriefProject>> = projectApi.getProjectList(
        platformId = platformId,
        projectType = json.encodeToJsonElement(type.toData()).jsonPrimitive.content,
        loaders = loaders?.let { loaders -> json.encodeToJsonElement(loaders.map { it.toMetaData() }).jsonArray.map { it.jsonPrimitive.content } },
        gameVersions = gameVersions,
        categories = categories,
        query = query,
        pageSize = pageSize,
    ).map { list -> list.results.map { it.toDomain() } }.toResult()

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
