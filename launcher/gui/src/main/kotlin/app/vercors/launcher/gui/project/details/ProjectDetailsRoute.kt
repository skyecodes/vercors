package app.vercors.launcher.gui.project.details

import app.vercors.launcher.gui.navigation.Route
import app.vercors.launcher.gui.navigation.TopLevelDestination
import kotlinx.serialization.Serializable

@Serializable
data class ProjectDetailsRoute(val platformId: String, val projectId: String) : Route {
    override val topLevelDestination get() = TopLevelDestination.Content
}
