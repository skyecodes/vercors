package app.vercors.feature.project.domain.usecase

import app.vercors.core.domain.Result
import app.vercors.feature.project.domain.model.BriefProject
import app.vercors.feature.project.domain.model.PlatformId
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.domain.repository.ProjectRepository
import dev.zacsweers.metro.Inject

@Inject
class GetFeaturedProjectListUseCase(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(platformId: PlatformId): Result<Map<ProjectType, List<BriefProject>>> =
        projectRepository.getFeaturedProjectList(platformId)
}
