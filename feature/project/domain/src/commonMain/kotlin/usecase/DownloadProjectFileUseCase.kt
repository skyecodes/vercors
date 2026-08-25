package app.vercors.feature.project.domain.usecase

import app.vercors.feature.project.domain.model.ProjectFileDownloadError
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.domain.repository.ProjectRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject

@Inject
class DownloadProjectFileUseCase(
    private val projectRepository: ProjectRepository
) {
    context(_: Raise<ProjectFileDownloadError>)
    suspend operator fun invoke(
        projectId: ProjectId,
        fileId: String,
        fileName: String,
        instanceId: String,
        type: ProjectType,
        size: Long?,
        sha1: String?,
        md5: String?
    ) =
        projectRepository.downloadProjectFile(projectId, fileId, fileName, instanceId, type, size, sha1, md5)
}
