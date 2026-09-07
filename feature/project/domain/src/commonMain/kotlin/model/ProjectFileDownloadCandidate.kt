package app.vercors.feature.project.domain.model

/**
 * One file to download as part of a batch [app.vercors.feature.project.domain.repository.ProjectRepository.downloadProjectFiles]
 * call. [type] is this specific file's own project type (e.g. a Mod dependency of a ResourcePack install still
 * carries [ProjectType.Mod]) — never assume every candidate in a batch shares the same type.
 */
data class ProjectFileDownloadCandidate(
    val projectId: ProjectId,
    val fileId: String,
    val fileName: String,
    val type: ProjectType,
    val size: Long?,
    val sha1: String?,
    val md5: String?
)
