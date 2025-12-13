package app.vercors.launcher.gui.home

import app.vercors.launcher.gui.navigation.Route
import app.vercors.launcher.gui.navigation.TopLevelDestination
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute : Route {
    override val topLevelDestination get() = TopLevelDestination.Home
}
