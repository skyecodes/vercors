package app.vercors.feature.project.domain.usecase

import app.vercors.feature.project.domain.model.ProjectFileDownloadCandidate
import app.vercors.feature.project.domain.model.ProjectFileDownloadError
import app.vercors.feature.project.domain.repository.ProjectRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject
import opensavvy.progress.Progress

@Inject
class InstallWorldFileUseCase(
    private val projectRepository: ProjectRepository
) {
    context(_: Raise<ProjectFileDownloadError>)
    suspend operator fun invoke(
        candidate: ProjectFileDownloadCandidate,
        instanceId: String,
        onProgress: (Progress) -> Unit = {}
    ): Unit = projectRepository.installWorldFile(candidate, instanceId, onProgress)
}
