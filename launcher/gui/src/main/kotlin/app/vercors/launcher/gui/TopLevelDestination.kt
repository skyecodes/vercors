package app.vercors.launcher.gui

import androidx.compose.ui.graphics.vector.ImageVector
import app.vercors.feature.account.ui.list.AccountListRoute
import app.vercors.feature.home.ui.HomeRoute
import app.vercors.feature.instance.ui.list.InstanceListRoute
import app.vercors.feature.project.ui.list.ProjectListRoute
import app.vercors.launcher.gui.config.ConfigurationRoute
import app.vercors.shared.ui.Route
import com.composables.icons.lucide.*
import org.jetbrains.compose.resources.StringResource

enum class TopLevelDestination(
    val route: Route,
    val text: StringResource,
    val icon: ImageVector
) {
    Home(
        route = HomeRoute,
        text = Res.string.home,
        icon = Lucide.House
    ),
    Instances(
        route = InstanceListRoute,
        text = Res.string.instances,
        icon = Lucide.Gamepad2
    ),
    Content(
        route = ProjectListRoute,
        text = Res.string.content,
        icon = Lucide.Box
    ),
    Settings(
        route = ConfigurationRoute,
        text = Res.string.settings,
        icon = Lucide.Settings
    ),
    Account(
        route = AccountListRoute,
        text = Res.string.account,
        icon = Lucide.User
    )
}
