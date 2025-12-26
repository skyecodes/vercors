package app.vercors.launcher.gui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.vercors.launcher.gui.navigation.Route
import app.vercors.launcher.gui.navigation.TopLevelDestination
import app.vercors.shared.ui.core.animation.AppAnimatedVisibility
import app.vercors.shared.ui.core.animation.appAnimateColorAsState
import app.vercors.shared.ui.core.animation.appAnimateDpAsState
import app.vercors.shared.ui.core.component.AppButton
import app.vercors.shared.ui.core.component.AppIcon
import app.vercors.shared.ui.core.component.AppText
import app.vercors.shared.ui.core.theme.background
import app.vercors.shared.ui.core.theme.colors
import app.vercors.shared.ui.core.theme.primary
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.stringResource
import pro.respawn.flowmvi.api.IntentReceiver

@Composable
context(store: IntentReceiver<AppIntent>)
fun AppMenu(currentBackStackEntry: Route?, isDrawerOpen: Boolean) {
    Box(
        modifier = Modifier.fillMaxHeight(),
        contentAlignment = Alignment.CenterEnd
    ) {
        AppMenuButtonList(currentBackStackEntry, isDrawerOpen)
        ToggleDrawerButton(isDrawerOpen)
    }
}

@Composable
context(store: IntentReceiver<AppIntent>)
private fun AppMenuButtonList(currentBackStackEntry: Route?, isDrawerOpen: Boolean) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxHeight().width(IntrinsicSize.Max)
            .padding(horizontal = 4.dp, vertical = 16.dp)
    ) {
        TopLevelDestination.entries.forEachIndexed { index, destination ->
            if (index == 3) {
                Spacer(Modifier.weight(1f))
            }
            AppMenuDestination(
                topLevelDestination = destination,
                currentBackStackEntry = currentBackStackEntry,
                isDrawerOpen = isDrawerOpen
            )
        }
    }
}

@Composable
context(store: IntentReceiver<AppIntent>)
private fun AppMenuDestination(
    topLevelDestination: TopLevelDestination,
    currentBackStackEntry: Route?,
    isDrawerOpen: Boolean
) {
    val backgroundColor by appAnimateColorAsState(
        if (topLevelDestination == currentBackStackEntry?.topLevelDestination) Theme[colors][primary].copy(
            alpha = 0.2f
        ) else Color.Unspecified
    )
    val horizontalPadding by appAnimateDpAsState(if (isDrawerOpen) 16.dp else 8.dp)
    val width by appAnimateDpAsState(if (isDrawerOpen) 140.dp else 40.dp)

    AppButton(
        onClick = { store.intent(AppIntent.Navigate(topLevelDestination.route)) },
        backgroundColor = backgroundColor,
        modifier = Modifier.width(width).height(40.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 8.dp)
    ) {
        AppIcon(
            imageVector = topLevelDestination.icon,
            contentDescription = stringResource(topLevelDestination.text)
        )
        AppAnimatedVisibility(
            visible = isDrawerOpen,
            modifier = Modifier.weight(1f)
        ) {
            AppText(
                text = stringResource(topLevelDestination.text),
                singleLine = true
            )
        }
    }
}

@Composable
context(store: IntentReceiver<AppIntent>)
private fun ToggleDrawerButton(isDrawerOpen: Boolean) {
    AppButton(
        onClick = { store.intent(AppIntent.ToggleDrawer) },
        modifier = Modifier.offset(x = 2.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
        backgroundColor = Theme[colors][background],
    ) {
        AppIcon(
            imageVector = if (isDrawerOpen) Lucide.ChevronLeft else Lucide.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
    }
}
