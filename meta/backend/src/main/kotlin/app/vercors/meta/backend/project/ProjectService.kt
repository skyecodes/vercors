package app.vercors.meta.backend.project

import app.vercors.meta.backend.project.curseforge.CurseForgeService
import app.vercors.meta.backend.project.modrinth.ModrinthService
import app.vercors.meta.lib.project.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

@Single
class ProjectService(
    curseForgeService: CurseForgeService,
    modrinthService: ModrinthService
) {
    private val platformServices: Map<String, PlatformServiceBase> =
        listOf(modrinthService, curseForgeService).associateBy { it.info.id }

    suspend fun getPlatformList(): List<PlatformData> =
        platformServices.values.map { it.getPlatformData() }.sortedBy { it.id }

    suspend fun getPlatformDetails(platformId: String): PlatformData = getPlatformService(platformId).getPlatformData()

    suspend fun getFeaturedProjectList(platformId: String): FeaturedProjectListData = withContext(Dispatchers.IO) {
        getPlatformService(platformId).getCategories().keys.map { projectType ->
            async {
                projectType to search(
                    platformId = platformId,
                    projectType = projectType
                ).results
            }
        }.awaitAll().toMap()
    }

    suspend fun search(
        platformId: String,
        projectType: ProjectTypeData? = null,
        loaders: List<ProjectLoaderData>? = null,
        gameVersions: List<String>? = null,
        categories: List<String>? = null,
        query: String? = null,
        firstIndex: Int? = 0,
        pageSize: Int? = 10
    ): ProjectSearchResponseData = getPlatformService(platformId).search(
        projectType = projectType,
        loaders = loaders,
        gameVersions = gameVersions,
        categories = categories,
        query = query,
        firstIndex = firstIndex,
        pageSize = pageSize,
    )

    suspend fun getProjectDetails(platformId: String, projectId: String) =
        getPlatformService(platformId).getProject(projectId)

    private fun getPlatformService(platformId: String): PlatformServiceBase =
        requireNotNull(platformServices[platformId]) { "Unknown platform: $platformId" }
}
