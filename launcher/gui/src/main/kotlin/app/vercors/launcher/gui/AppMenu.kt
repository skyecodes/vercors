package app.vercors.launcher.gui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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
import app.vercors.shared.ui.background
import app.vercors.shared.ui.colors
import app.vercors.shared.ui.component.AppButton
import app.vercors.shared.ui.component.AppIcon
import app.vercors.shared.ui.component.AppText
import app.vercors.shared.ui.primary
import com.composables.icons.lucide.*
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.stringResource
import pro.respawn.flowmvi.api.IntentReceiver

@Composable
context(store: IntentReceiver<AppIntent>)
fun AppMenu(currentBackStackEntry: Any?, isDrawerOpen: Boolean) {
    Box(
        modifier = Modifier.fillMaxHeight(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxHeight().width(IntrinsicSize.Max)
                .padding(horizontal = 4.dp, vertical = 16.dp)
        ) {
            AppMenuDestination(
                route = HomeRoute,
                text = stringResource(Res.string.home),
                icon = Lucide.House,
                currentBackStackEntry = currentBackStackEntry,
                isDrawerOpen = isDrawerOpen
            )
            AppMenuDestination(
                route = InstanceListRoute,
                text = stringResource(Res.string.instances),
                icon = Lucide.Gamepad2,
                currentBackStackEntry = currentBackStackEntry,
                isDrawerOpen = isDrawerOpen
            )
            AppMenuDestination(
                route = ProjectListRoute,
                text = stringResource(Res.string.content),
                icon = Lucide.Box,
                currentBackStackEntry = currentBackStackEntry,
                isDrawerOpen = isDrawerOpen
            )
            Spacer(Modifier.weight(1f))
            AppMenuDestination(
                route = ConfigurationRoute,
                text = stringResource(Res.string.settings),
                icon = Lucide.Settings,
                currentBackStackEntry = currentBackStackEntry,
                isDrawerOpen = isDrawerOpen
            )
            AppMenuDestination(
                route = AccountListRoute,
                text = stringResource(Res.string.account),
                icon = Lucide.User,
                currentBackStackEntry = currentBackStackEntry,
                isDrawerOpen = isDrawerOpen
            )
        }

        AppButton(
            onClick = { store.intent(AppIntent.ToggleDrawer) },
            modifier = Modifier.offset(x = 12.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            backgroundColor = Theme[colors][background],
        ) {
            AppIcon(
                imageVector = if (isDrawerOpen) Lucide.ChevronLeft else Lucide.ChevronRight,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp).size(16.dp)
            )
        }
    }
}

@Composable
context(store: IntentReceiver<AppIntent>)
private fun AppMenuDestination(
    route: Any,
    text: String,
    icon: ImageVector,
    currentBackStackEntry: Any?,
    isDrawerOpen: Boolean
) {
    val backgroundColor by animateColorAsState(if (route == currentBackStackEntry) Theme[colors][primary].copy(alpha = 0.2f) else Color.Unspecified)
    val horizontalPadding by animateDpAsState(if (isDrawerOpen) 16.dp else 8.dp)
    val width by animateDpAsState(if (isDrawerOpen) 150.dp else 40.dp)

    AppButton(
        onClick = { store.intent(AppIntent.Navigate(route)) },
        backgroundColor = backgroundColor,
        modifier = Modifier.width(width).height(40.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 8.dp)
    ) {
        AppIcon(imageVector = icon, contentDescription = text)
        AnimatedVisibility(
            visible = isDrawerOpen,
            modifier = Modifier.weight(1f)
        ) {
            AppText(text, singleLine = true)
        }
    }
}
