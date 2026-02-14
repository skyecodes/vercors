package app.vercors.feature.project.domain.repository

import app.vercors.core.domain.Page
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.project.domain.model.*
import arrow.core.raise.Raise

interface ProjectRepository {
    context(_: Raise<PlatformError>)
    suspend fun getPlatformList(): List<ProjectPlatform>

    context(_: Raise<PlatformNotFound>, _: Raise<PlatformError>)
    suspend fun getFeaturedProjectList(platformId: PlatformId): FeaturedProjectList

    context(_: Raise<ProjectSearchError>)
    suspend fun searchProjects(
        platformId: PlatformId,
        type: ProjectType,
        loaders: List<ModLoaderType>?,
        gameVersions: List<String>?,
        query: String?,
        categories: List<String>?,
        sortBy: ProjectSortBy?,
        pageSize: Int?,
        offset: Int?
    ): Page<BriefProject>

    context(_: Raise<ProjectNotFound>, _: Raise<PlatformNotFound>, _: Raise<PlatformError>)
    suspend fun getProject(projectId: ProjectId): Project
}
