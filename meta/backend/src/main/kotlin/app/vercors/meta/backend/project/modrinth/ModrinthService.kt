package app.vercors.meta.backend.project.modrinth

import app.vercors.meta.backend.project.PlatformServiceBase
import app.vercors.meta.lib.project.PlatformData
import app.vercors.meta.lib.project.ProjectData
import app.vercors.meta.lib.project.ProjectSearchResponseData
import app.vercors.meta.lib.project.ProjectTypeData
import org.koin.core.annotation.Single

internal const val MODRINTH_PLATFORM_ID = "modrinth"

@Single
class ModrinthService(
    private val api: ModrinthApi
) : PlatformServiceBase {
    override val info: PlatformData = PlatformData(
        id = MODRINTH_PLATFORM_ID,
        name = "Modrinth",
        baseUrl = "https://modrinth.com/"
    )

    override suspend fun search(
        type: ProjectTypeData,
        query: String?,
        limit: Int?
    ): ProjectSearchResponseData = api.search(
        query = query,
        facets = buildFacets(type),
        limit = limit
    ).convert()

    override suspend fun getProject(projectId: String): ProjectData = api.getProject(projectId).convert()
}
