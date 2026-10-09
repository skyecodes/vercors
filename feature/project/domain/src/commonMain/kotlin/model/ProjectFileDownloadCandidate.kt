package app.vercors.feature.project.domain.model

// type is per file: a dependency can differ from its parent's type
data class ProjectFileDownloadCandidate(
    val projectId: ProjectId,
    val fileId: String,
    val fileName: String,
    val type: ProjectType,
    val size: Long?,
    val sha1: String?,
    val md5: String?
)
