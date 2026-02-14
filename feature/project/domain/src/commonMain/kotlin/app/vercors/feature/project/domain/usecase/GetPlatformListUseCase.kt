package app.vercors.feature.project.domain.usecase

import app.vercors.feature.project.domain.model.PlatformError
import app.vercors.feature.project.domain.model.ProjectPlatform
import app.vercors.feature.project.domain.repository.ProjectRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject

@Inject
class GetPlatformListUseCase(
    private val projectRepository: ProjectRepository
) {
    context(_: Raise<PlatformError>)
    suspend operator fun invoke(): List<ProjectPlatform> = projectRepository.getPlatformList()
}
