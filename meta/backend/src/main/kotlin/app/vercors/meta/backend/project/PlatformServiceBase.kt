package app.vercors.meta.backend.project

import app.vercors.meta.lib.project.*

interface PlatformServiceBase {
    val info: PlatformInfo

    suspend fun getPlatformData(): PlatformData = PlatformData(
        id = info.id,
        name = info.name,
        baseUrl = info.baseUrl,
        categories = getCategories()
    )

    suspend fun getCategories(): Map<ProjectTypeData, List<ProjectCategoryData>>

    suspend fun search(
        projectType: ProjectTypeData?,
        loaders: List<ProjectLoaderData>?,
        gameVersions: List<String>?,
        categories: List<String>?,
        query: String?,
        firstIndex: Int?,
        pageSize: Int?
    ): ProjectSearchResponseData

    suspend fun getProject(projectId: String): ProjectData
}

data class PlatformInfo(
    val id: String,
    val name: String,
    val baseUrl: String
)
