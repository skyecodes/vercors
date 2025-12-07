package app.vercors.shared.domain.project.model

data class Project(
    val id: ProjectId,
    val name: String,
    val description: String,
    val body: String?,
    val url: String,
    val logoUrl: String?,
)
