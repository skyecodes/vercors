package app.vercors.feature.project.domain.usecase

import app.vercors.core.domain.Page
import app.vercors.core.domain.Result
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.project.domain.model.BriefProject
import app.vercors.feature.project.domain.model.PlatformId
import app.vercors.feature.project.domain.model.ProjectSortBy
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.domain.repository.ProjectRepository
import dev.zacsweers.metro.Inject

@Inject
class GetProjectListUseCase(
    private val projectRepository: ProjectRepository
) {
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
    ): Result<Page<BriefProject>> = projectRepository.getProjectList(
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
