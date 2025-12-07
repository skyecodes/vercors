package app.vercors.shared.domain.project.usecase

import app.vercors.shared.domain.Result
import app.vercors.shared.domain.project.ProjectRepository
import app.vercors.shared.domain.project.model.BriefProject
import app.vercors.shared.domain.project.model.ProjectPlatform
import app.vercors.shared.domain.project.model.ProjectType
import org.koin.core.annotation.Single

@Single
class GetFeaturedProjectListUseCase(private val projectRepository: ProjectRepository) {
    suspend operator fun invoke(platform: ProjectPlatform): Result<Map<ProjectType, List<BriefProject>>> =
        projectRepository.getFeaturedProjectList(platform)
}
