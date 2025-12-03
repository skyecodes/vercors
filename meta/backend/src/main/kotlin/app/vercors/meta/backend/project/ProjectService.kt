package app.vercors.meta.backend.project

import app.vercors.meta.backend.project.curseforge.CurseForgeService
import app.vercors.meta.backend.project.modrinth.ModrinthService
import app.vercors.meta.lib.project.FeaturedProjectListData
import app.vercors.meta.lib.project.PlatformData
import app.vercors.meta.lib.project.ProjectSearchResponseData
import app.vercors.meta.lib.project.ProjectTypeData
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

    fun getPlatformList(): List<PlatformData> = platformServices.values.map { it.info }.sortedBy { it.id }

    fun getProjectTypeList(): List<ProjectTypeData> = ProjectTypeData.entries

    fun getPlatformDetails(platformId: String): PlatformData = getPlatformService(platformId).info

    suspend fun getFeaturedProjectList(platformId: String): FeaturedProjectListData = withContext(Dispatchers.IO) {
        ProjectTypeData.entries.map { projectType ->
            async {
                projectType to search(
                    platformId = platformId,
                    projectType = projectType,
                    limit = 10
                ).results
            }
        }.awaitAll().toMap()
    }

    suspend fun search(
        platformId: String,
        projectType: ProjectTypeData,
        query: String? = null,
        limit: Int? = null
    ): ProjectSearchResponseData = getPlatformService(platformId).search(
        type = projectType,
        query = query,
        limit = limit,
    )

    private fun getPlatformService(platformId: String): PlatformServiceBase =
        requireNotNull(platformServices[platformId]) { "Unknown platform: $platformId" }
}