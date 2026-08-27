package app.vercors.feature.project.domain.model

/** One file to download as part of a batch [app.vercors.feature.project.domain.repository.ProjectRepository.downloadProjectFiles] call. */
data class ProjectFileDownloadCandidate(
    val projectId: ProjectId,
    val fileId: String,
    val fileName: String,
    val size: Long?,
    val sha1: String?,
    val md5: String?
)
