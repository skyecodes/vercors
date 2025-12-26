package app.vercors.shared.domain.project

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
        pageSize: Int?
    ): Result<List<BriefProject>>

    suspend fun getProject(projectId: ProjectId): Result<Project>
}
