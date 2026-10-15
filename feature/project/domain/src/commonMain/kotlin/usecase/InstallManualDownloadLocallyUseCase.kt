package app.vercors.feature.project.domain.usecase

import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.domain.repository.ProjectRepository
import dev.zacsweers.metro.Inject

@Inject
class InstallManualDownloadLocallyUseCase(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(
        fileName: String,
        instanceId: String,
        type: ProjectType,
        size: Long?,
        sha1: String?,
        md5: String?
    ): Boolean = projectRepository.installManualDownloadLocally(fileName, instanceId, type, size, sha1, md5)
}
