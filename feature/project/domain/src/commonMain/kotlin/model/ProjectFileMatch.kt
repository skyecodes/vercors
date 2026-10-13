package app.vercors.feature.project.domain.model

data class ProjectFileMatch(
    val sha1: String,
    val projectId: ProjectId,
    val file: ProjectFile
)
