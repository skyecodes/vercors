package app.vercors.launcher.gui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import app.vercors.core.ui.animation.LocalAnimation
import app.vercors.core.ui.theme.AppTheme
import app.vercors.core.ui.theme.background
import app.vercors.core.ui.theme.backgroundAlt
import app.vercors.core.ui.theme.colors
import app.vercors.feature.home.ui.HomeScreen
import app.vercors.shared.ui.Screen
import app.vercors.shared.ui.Store
import com.composeunstyled.theme.Theme
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.sharedelements.SharedElementTransitionLayout
import dev.zacsweers.metro.AppScope

@CircuitInject(AppScreen::class, AppScope::class)
@Composable
fun App(
    store: Store<AppState, AppEvent>,
    modifier: Modifier = Modifier,
) {
    val (state, eventSink) = store

    if (state.guiConfig != null) {
        CompositionLocalProvider(LocalAnimation provides state.guiConfig.animations) {
            AppTheme {
                Box(
                    modifier = modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().background(Theme[colors][backgroundAlt])
                    ) {
                        val backStack = rememberSaveableBackStack(root = HomeScreen)
                        val navigator = rememberCircuitNavigator(backStack) {}

                        AppTopBar(
                            canGoBack = backStack.size > 1,
                            canGoNext = false, // TODO
                            isMinimized = state.isMinimized,
                            onWindowEvent = state.onWindowEvent,
                            onNavigateBack = { navigator.pop() },
                            onNavigateNext = { } // TODO
                        )
                        Row {
                            AppMenu(
                                currentBackStackEntry = backStack.topRecord?.screen as? Screen,
                                isDrawerOpen = state.guiConfig.menuDrawerOpen,
                                onToggleDrawerClick = { eventSink(AppEvent.ToggleDrawer) },
                                onMenuButtonClick = { navigator.goTo(it) }
                            )
                            AppContent(backStack, navigator)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun AppContent(
    backStack: SaveableBackStack,
    navigator: Navigator
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(end = 4.dp, top = 4.dp, bottom = 4.dp)
            .background(
                color = Theme[colors][background],
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
    ) {
        SharedElementTransitionLayout {
            NavigableCircuitContent(
                navigator = navigator,
                backStack = backStack
            )
        }
    }
}

sealed interface AppWindowEvent {
    data object Minimize : AppWindowEvent
    data object Maximize : AppWindowEvent
    data object Close : AppWindowEvent
}

internal val LocalAppWindowScope = staticCompositionLocalOf<WindowScope?> { null }
