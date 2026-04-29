package app.vercors.feature.project.data.repository

import app.vercors.core.domain.Page
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.project.api.Platforms
import app.vercors.feature.project.api.Projects
import app.vercors.feature.project.api.dto.PlatformNotFoundData
import app.vercors.feature.project.api.dto.ProjectLoaderData
import app.vercors.feature.project.api.dto.ProjectNotFoundData
import app.vercors.feature.project.data.toData
import app.vercors.feature.project.data.toDomain
import app.vercors.feature.project.domain.model.*
import app.vercors.feature.project.domain.repository.ProjectRepository
import app.vercors.shared.api.ApiV1
import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import opensavvy.spine.api.div
import opensavvy.spine.api.invoke
import opensavvy.spine.client.handle
import opensavvy.spine.client.request

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class ProjectRepositoryImpl(
    private val httpClient: HttpClient,
    private val json: Json
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

    context(_: Raise<ProjectNotFound>, _: Raise<PlatformNotFound>, _: Raise<PlatformError>)
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
}

private fun ModLoaderType.toMetaData() = when (this) {
    ModLoaderType.Forge -> ProjectLoaderData.Forge
    ModLoaderType.Quilt -> ProjectLoaderData.Quilt
    ModLoaderType.Fabric -> ProjectLoaderData.Fabric
    ModLoaderType.NeoForge -> ProjectLoaderData.NeoForge
}
