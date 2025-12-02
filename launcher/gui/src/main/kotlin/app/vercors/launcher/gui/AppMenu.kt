package app.vercors.launcher.gui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.vercors.launcher.gui.account.AccountListRoute
import app.vercors.launcher.gui.config.ConfigurationRoute
import app.vercors.launcher.gui.home.HomeRoute
import app.vercors.launcher.gui.instance.list.InstanceListRoute
import app.vercors.launcher.gui.project.list.ProjectListRoute
import app.vercors.launcher.gui.res.*
import app.vercors.shared.ui.backgroundAlt
import app.vercors.shared.ui.colors
import app.vercors.shared.ui.component.AppButton
import app.vercors.shared.ui.component.AppIcon
import app.vercors.shared.ui.component.AppText
import app.vercors.shared.ui.onPrimary
import app.vercors.shared.ui.primary
import com.composables.icons.lucide.*
import com.composeunstyled.LocalContentColor
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.stringResource
import pro.respawn.flowmvi.api.IntentReceiver

@Composable
context(store: IntentReceiver<AppIntent>)
fun AppMenu(currentBackStackEntry: Any?) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxHeight().width(IntrinsicSize.Max).background(Theme[colors][backgroundAlt])
            .padding(horizontal = 4.dp, vertical = 16.dp)
    ) {
        AppMenuDestination(
            route = HomeRoute,
            text = stringResource(Res.string.home),
            icon = Lucide.House,
            currentBackStackEntry = currentBackStackEntry
        )
        AppMenuDestination(
            route = InstanceListRoute,
            text = stringResource(Res.string.instances),
            icon = Lucide.Gamepad2,
            currentBackStackEntry = currentBackStackEntry
        )
        AppMenuDestination(
            route = ProjectListRoute,
            text = stringResource(Res.string.content),
            icon = Lucide.Box,
            currentBackStackEntry = currentBackStackEntry
        )
        Spacer(Modifier.weight(1f))
        AppMenuDestination(
            route = ConfigurationRoute,
            text = stringResource(Res.string.settings),
            icon = Lucide.Settings,
            currentBackStackEntry = currentBackStackEntry
        )
        AppMenuDestination(
            route = AccountListRoute,
            text = stringResource(Res.string.account),
            icon = Lucide.User,
            currentBackStackEntry = currentBackStackEntry
        )
    }
}

@Composable
context(store: IntentReceiver<AppIntent>)
private fun AppMenuDestination(
    route: Any,
    text: String,
    icon: ImageVector,
    currentBackStackEntry: Any?
) {
    val backgroundColor by animateColorAsState(if (route == currentBackStackEntry) Theme[colors][primary] else Color.Unspecified)
    val contentColor by animateColorAsState(if (route == currentBackStackEntry) Theme[colors][onPrimary] else LocalContentColor.current)

    AppButton(
        onClick = { store.intent(AppIntent.Navigate(route)) },
        backgroundColor = backgroundColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(imageVector = icon, contentDescription = text)
            Spacer(Modifier.width(8.dp))
            AppText(text)
        }
    }
}