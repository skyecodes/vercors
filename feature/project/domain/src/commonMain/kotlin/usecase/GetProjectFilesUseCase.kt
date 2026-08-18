package app.vercors.feature.project.domain.usecase

import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.project.domain.model.ProjectDetailsError
import app.vercors.feature.project.domain.model.ProjectFile
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.domain.repository.ProjectRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject

@Inject
class GetProjectFilesUseCase(
    private val projectRepository: ProjectRepository
) {
    context(_: Raise<ProjectDetailsError>)
    suspend operator fun invoke(
        projectId: ProjectId,
        gameVersion: String? = null,
        loader: ModLoaderType? = null
    ): List<ProjectFile> = projectRepository.getProjectFiles(projectId, gameVersion, loader)
}
