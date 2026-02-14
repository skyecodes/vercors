package app.vercors.feature.project.domain.model

sealed interface ProjectSearchError

data class UnsupportedProjectType(val projectType: ProjectType) : ProjectSearchError

data class ProjectNotFound(val projectId: ProjectId)

data class PlatformNotFound(val platformId: PlatformId) : ProjectSearchError

data class PlatformError(
    val error: String,
    val description: String?
) : ProjectSearchError
