package app.vercors.feature.project.domain.repository

import app.vercors.core.domain.Page
import app.vercors.core.domain.Result
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.project.domain.model.*

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
