package app.vercors.launcher.gui.config

import app.vercors.launcher.gui.navigation.Route
import app.vercors.launcher.gui.navigation.TopLevelDestination
import kotlinx.serialization.Serializable

@Serializable
data object ConfigurationRoute : Route {
    override val topLevelDestination get() = TopLevelDestination.Settings
}
