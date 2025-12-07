package app.vercors.shared.domain.project.model

data class ProjectId(
    val id: String,
    val platform: ProjectPlatform
) {
    constructor(id: String, platformId: String) : this(id, ProjectPlatform.entries.first { it.id == platformId })
}
