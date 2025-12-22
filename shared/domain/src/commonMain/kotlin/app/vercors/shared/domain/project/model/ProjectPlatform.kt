package app.vercors.shared.domain.project.model

data class ProjectPlatform(
    val id: PlatformId,
    val name: String,
    val baseUrl: String,
    val categories: Map<ProjectType, Map<String, List<ProjectCategory>>>
)

typealias PlatformId = String
