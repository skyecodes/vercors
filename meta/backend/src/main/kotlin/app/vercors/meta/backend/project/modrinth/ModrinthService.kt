package app.vercors.meta.backend.project.modrinth

import app.vercors.meta.backend.project.PlatformInfo
import app.vercors.meta.backend.project.PlatformServiceBase
import app.vercors.meta.lib.project.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.koin.core.annotation.Single

internal const val MODRINTH_PLATFORM_ID = "modrinth"

@Single
class ModrinthService(
    private val api: ModrinthApi
) : PlatformServiceBase {
    override val info: PlatformInfo = PlatformInfo(
        id = MODRINTH_PLATFORM_ID,
        name = "Modrinth",
        baseUrl = "https://modrinth.com/"
    )

    override suspend fun getCategories(): Map<ProjectTypeData, List<ProjectCategoryData>> = coroutineScope {
        val types = async { api.getProjectTypes().mapNotNull { it.toModrinthProjectType() } }
        val categories = async { api.getCategories() }

        categories.await().groupBy { it.projectType.convert() }
            .filterKeys { it in types.await() }
            .map { (k, v) -> k!! to v.map { it.convert() } }
            .toMap()
    }

    override suspend fun search(
        projectType: ProjectTypeData?,
        loaders: List<ProjectLoaderData>?,
        gameVersions: List<String>?,
        categories: List<String>?,
        query: String?,
        firstIndex: Int?,
        pageSize: Int?
    ): ProjectSearchResponseData = api.search(
        query = query,
        facets = buildFacets(
            projectType = projectType,
            loaders = loaders,
            gameVersions = gameVersions,
            categories = categories
        ),
        limit = pageSize
    ).convert()

    override suspend fun getProject(projectId: String): ProjectData = api.getProject(projectId).convert()
}
