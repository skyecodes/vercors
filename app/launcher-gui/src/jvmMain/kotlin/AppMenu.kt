package app.vercors.launcher.gui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.animation.AppAnimatedVisibility
import app.vercors.core.ui.animation.appAnimateColorAsState
import app.vercors.core.ui.animation.appAnimateDpAsState
import app.vercors.core.ui.component.AppButton
import app.vercors.core.ui.component.AppIcon
import app.vercors.core.ui.component.AppText
import app.vercors.core.ui.theme.background
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.primary
import app.vercors.feature.account.ui.list.AccountListScreen
import app.vercors.feature.config.ui.ConfigurationScreen
import app.vercors.feature.home.ui.HomeScreen
import app.vercors.feature.instance.ui.details.InstanceDetailsScreen
import app.vercors.feature.instance.ui.list.InstanceListScreen
import app.vercors.feature.project.ui.details.ProjectDetailsScreen
import app.vercors.feature.project.ui.list.ProjectListScreen
import app.vercors.shared.ui.Screen
import com.composables.icons.lucide.*
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppMenu(
    currentBackStackEntry: Screen?,
    isDrawerOpen: Boolean,
    onToggleDrawerClick: () -> Unit,
    onMenuButtonClick: (Screen) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxHeight(),
        contentAlignment = Alignment.CenterEnd
    ) {
        AppMenuButtonList(
            currentBackStackEntry = currentBackStackEntry,
            isDrawerOpen = isDrawerOpen,
            onMenuButtonClick = onMenuButtonClick
        )
        ToggleDrawerButton(
            isDrawerOpen = isDrawerOpen,
            onToggleDrawerClick = onToggleDrawerClick
        )
    }
}

@Composable
private fun AppMenuButtonList(
    currentBackStackEntry: Screen?,
    isDrawerOpen: Boolean,
    onMenuButtonClick: (Screen) -> Unit
) {
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
                isDrawerOpen = isDrawerOpen,
                onMenuButtonClick = onMenuButtonClick
            )
        }
    }
}

@Composable
private fun AppMenuDestination(
    topLevelDestination: TopLevelDestination,
    currentBackStackEntry: Screen?,
    isDrawerOpen: Boolean,
    onMenuButtonClick: (Screen) -> Unit
) {
    val backgroundColor by appAnimateColorAsState(
        if (topLevelDestination == currentBackStackEntry?.topLevelDestination)
            Theme[colors][primary].copy(alpha = 0.2f)
        else Color.Unspecified
    )
    val horizontalPadding by appAnimateDpAsState(if (isDrawerOpen) 16.dp else 8.dp)
    val width by appAnimateDpAsState(if (isDrawerOpen) 140.dp else 40.dp)

    AppButton(
        onClick = { onMenuButtonClick(topLevelDestination.screen) },
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
private fun ToggleDrawerButton(
    isDrawerOpen: Boolean,
    onToggleDrawerClick: () -> Unit
) {
    AppButton(
        onClick = onToggleDrawerClick,
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

private val Screen.topLevelDestination: TopLevelDestination?
    get() = when (this) {
        HomeScreen -> TopLevelDestination.Home
        InstanceListScreen, is InstanceDetailsScreen -> TopLevelDestination.Instances
        is ProjectListScreen, is ProjectDetailsScreen -> TopLevelDestination.Content
        AccountListScreen -> TopLevelDestination.Account
        ConfigurationScreen -> TopLevelDestination.Settings
        else -> null
    }

private enum class TopLevelDestination(
    val screen: Screen,
    val text: StringResource,
    val icon: ImageVector
) {
    Home(
        screen = HomeScreen,
        text = Res.string.home,
        icon = Lucide.House
    ),
    Instances(
        screen = InstanceListScreen,
        text = Res.string.instances,
        icon = Lucide.Gamepad2
    ),
    Content(
        screen = ProjectListScreen(),
        text = Res.string.content,
        icon = Lucide.Box
    ),
    Settings(
        screen = ConfigurationScreen,
        text = Res.string.settings,
        icon = Lucide.Settings
    ),
    Account(
        screen = AccountListScreen,
        text = Res.string.account,
        icon = Lucide.User
    )
}
