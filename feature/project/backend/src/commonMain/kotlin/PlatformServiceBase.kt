package app.vercors.feature.project.backend

import app.vercors.feature.project.api.dto.*
import arrow.core.raise.Raise

interface PlatformServiceBase {
    val info: PlatformInfo

    context(_: Raise<PlatformErrorData>)
    suspend fun getPlatformData(): ProjectPlatformData = ProjectPlatformData(
        id = info.id,
        name = info.name,
        baseUrl = info.baseUrl,
        categories = getCategories()
    )

    context(_: Raise<PlatformErrorData>)
    suspend fun getCategories(): Map<ProjectTypeData, Map<String, List<ProjectCategoryData>>>

    context(_: Raise<UnsupportedProjectTypeData>, _: Raise<PlatformErrorData>)
    suspend fun search(
        projectType: ProjectTypeData?,
        loaders: List<ProjectLoaderData>?,
        gameVersions: List<String>?,
        categories: List<String>?,
        query: String?,
        sortBy: ProjectSortByData?,
        offset: Int?,
        pageSize: Int?
    ): ProjectSearchResponseData

    context(_: Raise<PlatformErrorData>)
    suspend fun getProject(projectId: String): ProjectData

    context(_: Raise<PlatformErrorData>)
    suspend fun getProjectFiles(
        projectId: String,
        gameVersion: String?,
        loader: ProjectLoaderData?
    ): List<ProjectFileData>

    context(_: Raise<PlatformErrorData>)
    suspend fun getFileDownloadUrl(projectId: String, fileId: String): String

    context(_: Raise<PlatformErrorData>)
    suspend fun getProjectFile(projectId: String, fileId: String): ProjectFileData
}

data class PlatformInfo(
    val id: String,
    val name: String,
    val baseUrl: String
)
