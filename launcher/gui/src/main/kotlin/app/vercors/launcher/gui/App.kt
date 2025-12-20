package app.vercors.launcher.gui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import androidx.navigation3.ui.NavDisplay
import app.vercors.launcher.gui.navigation.LocalNavigator
import app.vercors.launcher.gui.navigation.Navigator
import app.vercors.launcher.gui.navigation.Route
import app.vercors.shared.ui.animation.LocalAnimation
import app.vercors.shared.ui.animation.LocalSharedTransitionScope
import app.vercors.shared.ui.animation.shouldAnimate
import app.vercors.shared.ui.theme.AppTheme
import app.vercors.shared.ui.theme.background
import app.vercors.shared.ui.theme.backgroundAlt
import app.vercors.shared.ui.theme.colors
import com.composeunstyled.theme.Theme
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.dsl.subscribe

@Composable
context(_: WindowScope)
fun App(
    container: AppContainer,
    onWindowEvent: (AppWindowEvent) -> Unit,
    isMinimized: Boolean
) = with(container.store) {
    val state by subscribe { _ -> }

    val navigator = remember {
        object : Navigator {
            override fun navigateTo(route: Route) {
                intent(AppIntent.Navigate(route))
            }

            override fun navigateBack() {
                intent(AppIntent.Back)
            }

            override fun navigateNext() {
                intent(AppIntent.Next)
            }
        }
    }

    CompositionLocalProvider(LocalNavigator provides navigator) {
        App(
            state = state,
            isMinimized = isMinimized,
            onWindowEvent = onWindowEvent
        )
    }
}

@Composable
context(_: WindowScope, _: IntentReceiver<AppIntent>)
private fun App(state: AppState, isMinimized: Boolean, onWindowEvent: (AppWindowEvent) -> Unit) {
    if (state.guiConfig != null) {
        CompositionLocalProvider(LocalAnimation provides state.guiConfig.animations) {
            AppTheme {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().background(Theme[colors][backgroundAlt])
                    ) {
                        AppTopBar(
                            canGoBack = state.canGoBack,
                            canGoNext = state.canGoNext,
                            isMinimized = isMinimized,
                            onWindowEvent = onWindowEvent
                        )
                        Row {
                            AppMenu(state.currentBackStackEntry, state.guiConfig.menuDrawerOpen)
                            AppContent(state.backStack)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(KoinExperimentalAPI::class)
@Composable
context(store: IntentReceiver<AppIntent>)
private fun AppContent(backStack: List<Any>) {
    val entryProvider = koinEntryProvider()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(end = 4.dp, top = 4.dp, bottom = 4.dp)
            .background(
                color = Theme[colors][background],
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        SharedTransitionLayout {
            CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                val shouldAnimate = shouldAnimate()
                NavDisplay(
                    backStack = backStack,
                    onBack = { store.intent(AppIntent.Back) },
                    entryProvider = entryProvider,
                    transitionSpec = { if (shouldAnimate) fadeIn() togetherWith fadeOut() else EnterTransition.None togetherWith ExitTransition.None }
                )
            }
        }
    }
}

sealed interface AppWindowEvent {
    data object Minimize : AppWindowEvent
    data object Maximize : AppWindowEvent
    data object Close : AppWindowEvent
}
