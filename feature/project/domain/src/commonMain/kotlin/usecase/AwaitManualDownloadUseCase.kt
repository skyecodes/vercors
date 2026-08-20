package app.vercors.feature.project.domain.usecase

import app.vercors.feature.project.domain.model.ManualDownloadError
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.domain.repository.ProjectRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject

@Inject
class AwaitManualDownloadUseCase(
    private val projectRepository: ProjectRepository
) {
    context(_: Raise<ManualDownloadError>)
    suspend operator fun invoke(fileName: String, instanceId: String, type: ProjectType) =
        projectRepository.awaitManualDownload(fileName, instanceId, type)
}
