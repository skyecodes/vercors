package app.vercors.feature.project.domain.usecase

import app.vercors.core.domain.Result
import app.vercors.feature.project.domain.model.ProjectPlatform
import app.vercors.feature.project.domain.repository.ProjectRepository
import dev.zacsweers.metro.Inject

@Inject
class GetPlatformListUseCase(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(): Result<List<ProjectPlatform>> = projectRepository.getPlatformList()
}
