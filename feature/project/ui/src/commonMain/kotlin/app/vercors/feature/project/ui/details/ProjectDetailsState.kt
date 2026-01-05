package app.vercors.feature.project.ui.details

import androidx.compose.runtime.Immutable
import app.vercors.feature.project.domain.model.ProjectId

@Immutable
data class ProjectDetailsState(
    val projectId: ProjectId
)
