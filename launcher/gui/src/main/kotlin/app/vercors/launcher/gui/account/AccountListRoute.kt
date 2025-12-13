package app.vercors.launcher.gui.account

import app.vercors.launcher.gui.navigation.Route
import app.vercors.launcher.gui.navigation.TopLevelDestination
import kotlinx.serialization.Serializable

@Serializable
data object AccountListRoute : Route {
    override val topLevelDestination get() = TopLevelDestination.Account
}
