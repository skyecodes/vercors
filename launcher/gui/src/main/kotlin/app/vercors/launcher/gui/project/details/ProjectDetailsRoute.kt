package app.vercors.launcher.gui.project.details

import app.vercors.launcher.gui.navigation.Route
import app.vercors.launcher.gui.navigation.TopLevelDestination
import app.vercors.shared.domain.project.model.ProjectId
import kotlinx.serialization.Serializable

@Serializable
data class ProjectDetailsRoute(val projectId: ProjectId) : Route {
    override val topLevelDestination get() = TopLevelDestination.Content
}
