package app.vercors.feature.project.domain.model

sealed interface ProjectSearchError

sealed interface ProjectDetailsError

data class UnsupportedProjectType(val projectType: ProjectType) : ProjectSearchError

data class ProjectNotFound(val projectId: ProjectId) : ProjectDetailsError

data class PlatformNotFound(val platformId: PlatformId) : ProjectSearchError, ProjectDetailsError

data class PlatformError(
    val error: String,
    val description: String?
) : ProjectSearchError, ProjectDetailsError
