package app.vercors.feature.project.ui.details

import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.shared.ui.Route
import kotlinx.serialization.Serializable

@Serializable
data class ProjectDetailsRoute(val projectId: ProjectId) : Route
