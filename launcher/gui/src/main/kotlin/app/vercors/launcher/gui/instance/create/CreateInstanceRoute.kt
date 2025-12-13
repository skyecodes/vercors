package app.vercors.launcher.gui.instance.create

import app.vercors.launcher.gui.navigation.Route
import app.vercors.launcher.gui.navigation.TopLevelDestination
import kotlinx.serialization.Serializable

@Serializable
data object CreateInstanceRoute : Route {
    override val topLevelDestination get() = TopLevelDestination.Instances
}
