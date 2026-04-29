package app.vercors.feature.project.domain.usecase

import app.vercors.core.domain.Page
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.project.domain.model.*
import app.vercors.feature.project.domain.repository.ProjectRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject

@Inject
class SearchProjectsUseCase(
    private val projectRepository: ProjectRepository
) {
    context(_: Raise<ProjectSearchError>)
    suspend operator fun invoke(
        platformId: PlatformId,
        type: ProjectType,
        loaders: List<ModLoaderType>?,
        gameVersions: List<String>?,
        query: String?,
        categories: List<String>?,
        sortBy: ProjectSortBy?,
        pageSize: Int?,
        offset: Int?
    ): Page<BriefProject> = projectRepository.searchProjects(
        platformId = platformId,
        type = type,
        loaders = loaders,
        gameVersions = gameVersions,
        query = query,
        categories = categories,
        sortBy = sortBy,
        pageSize = pageSize,
        offset = offset
    )
}
