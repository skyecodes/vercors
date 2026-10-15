package app.vercors.feature.project.domain.model

data class ProjectInstallCandidate(
    val projectId: ProjectId,
    val type: ProjectType,
    val fileId: String,
    val fileName: String,
    val size: Long?,
    val sha1: String?,
    val md5: String?,
    val manualDownloadUrl: String?,
)
