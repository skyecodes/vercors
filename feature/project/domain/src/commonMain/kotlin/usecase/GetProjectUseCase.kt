package app.vercors.feature.project.domain.usecase

import app.vercors.feature.project.domain.model.Project
import app.vercors.feature.project.domain.model.ProjectDetailsError
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.domain.repository.ProjectRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject

@Inject
class GetProjectUseCase(
    private val projectRepository: ProjectRepository
) {
    context(_: Raise<ProjectDetailsError>)
    suspend operator fun invoke(projectId: ProjectId): Project = projectRepository.getProject(projectId)
}
