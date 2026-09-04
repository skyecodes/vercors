package app.vercors.feature.project.domain.usecase

import app.vercors.feature.project.domain.model.ManualDownloadError
import app.vercors.feature.project.domain.repository.ProjectRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject

@Inject
class AwaitManualWorldDownloadUseCase(
    private val projectRepository: ProjectRepository
) {
    context(_: Raise<ManualDownloadError>)
    suspend operator fun invoke(
        fileName: String,
        instanceId: String,
        size: Long?,
        sha1: String?,
        md5: String?
    ) = projectRepository.awaitManualWorldDownload(fileName, instanceId, size, sha1, md5)
}
