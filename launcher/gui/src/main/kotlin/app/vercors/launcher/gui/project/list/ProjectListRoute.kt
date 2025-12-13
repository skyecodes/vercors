package app.vercors.launcher.gui.project.list

import app.vercors.launcher.gui.navigation.Route
import app.vercors.launcher.gui.navigation.TopLevelDestination
import kotlinx.serialization.Serializable

@Serializable
data object ProjectListRoute : Route {
    override val topLevelDestination get() = TopLevelDestination.Content
}
