package app.vercors.shared.domain.project.usecase

import app.vercors.shared.domain.Result
import app.vercors.shared.domain.project.ProjectRepository
import app.vercors.shared.domain.project.model.ProjectPlatform
import org.koin.core.annotation.Factory

@Factory
class GetPlatformListUseCase(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(): Result<List<ProjectPlatform>> = projectRepository.getPlatformList()
}
