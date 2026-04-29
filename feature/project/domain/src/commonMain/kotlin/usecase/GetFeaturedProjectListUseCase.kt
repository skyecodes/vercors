package app.vercors.feature.project.domain.usecase

import app.vercors.feature.project.domain.model.FeaturedProjectList
import app.vercors.feature.project.domain.model.PlatformError
import app.vercors.feature.project.domain.model.PlatformId
import app.vercors.feature.project.domain.model.PlatformNotFound
import app.vercors.feature.project.domain.repository.ProjectRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject

@Inject
class GetFeaturedProjectListUseCase(
    private val projectRepository: ProjectRepository
) {
    context(_: Raise<PlatformNotFound>, _: Raise<PlatformError>)
    suspend operator fun invoke(platformId: PlatformId): FeaturedProjectList =
        projectRepository.getFeaturedProjectList(platformId)
}
