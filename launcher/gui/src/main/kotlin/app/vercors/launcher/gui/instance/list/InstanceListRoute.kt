package app.vercors.launcher.gui.instance.list

import app.vercors.launcher.gui.navigation.Route
import app.vercors.launcher.gui.navigation.TopLevelDestination
import kotlinx.serialization.Serializable

@Serializable
data object InstanceListRoute : Route {
    override val topLevelDestination get() = TopLevelDestination.Instances
}
