package app.vercors.launcher.gui.instance.details

import app.vercors.launcher.common.domain.instance.model.InstanceId
import app.vercors.launcher.gui.navigation.Route
import app.vercors.launcher.gui.navigation.TopLevelDestination
import kotlinx.serialization.Serializable

@Serializable
data class InstanceDetailsRoute(val instanceId: InstanceId) : Route {
    override val topLevelDestination get() = TopLevelDestination.Instances
}
