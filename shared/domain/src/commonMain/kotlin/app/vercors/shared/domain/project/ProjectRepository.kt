package app.vercors.shared.domain.project

import app.vercors.shared.domain.Page
import app.vercors.shared.domain.Result
import app.vercors.shared.domain.loader.ModLoaderType
import app.vercors.shared.domain.project.model.*

interface ProjectRepository {
    suspend fun getPlatformList(): Result<List<ProjectPlatform>>
    suspend fun getFeaturedProjectList(platformId: PlatformId): Result<Map<ProjectType, List<BriefProject>>>
    suspend fun getProjectList(
        platformId: PlatformId,
        type: ProjectType,
        loaders: List<ModLoaderType>?,
        gameVersions: List<String>?,
        query: String?,
        categories: List<String>?,
        sortBy: ProjectSortBy?,
        pageSize: Int?,
        offset: Int?
    ): Result<Page<BriefProject>>

    suspend fun getProject(projectId: ProjectId): Result<Project>
}
