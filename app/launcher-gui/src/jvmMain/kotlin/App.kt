package app.vercors.launcher.gui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.animation.LocalAnimation
import app.vercors.core.ui.theme.AppTheme
import app.vercors.core.ui.theme.background
import app.vercors.core.ui.theme.backgroundAlt
import app.vercors.core.ui.theme.colors
import app.vercors.feature.home.ui.HomeScreen
import app.vercors.shared.ui.Screen
import app.vercors.shared.ui.Store
import com.composeunstyled.theme.Theme
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.navstack.rememberSaveableNavStack
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.navigation.NavStack
import com.slack.circuit.sharedelements.SharedElementTransitionLayout
import dev.nucleusframework.application.LocalNucleusWindow
import dev.nucleusframework.taskbarprogress.tao.hideTaskbarProgress
import dev.nucleusframework.taskbarprogress.tao.showTaskbarIndeterminate
import dev.nucleusframework.taskbarprogress.tao.showTaskbarProgress
import dev.zacsweers.metro.AppScope

@CircuitInject(AppScreen::class, AppScope::class)
@Composable
fun App(
    store: Store<AppState, AppEvent>,
    modifier: Modifier = Modifier,
) {
    val (state, eventSink) = store

    val navStack = rememberSaveableNavStack(root = HomeScreen)
    val navigator = rememberCircuitNavigator(navStack) {}

    val nucleusWindow = LocalNucleusWindow.current
    LaunchedEffect(state.installTaskbarProgress) {
        when (val progress = state.installTaskbarProgress) {
            InstallTaskbarProgress.None -> nucleusWindow.hideTaskbarProgress()
            InstallTaskbarProgress.Indeterminate -> nucleusWindow.showTaskbarIndeterminate()
            is InstallTaskbarProgress.Fraction -> nucleusWindow.showTaskbarProgress(progress.value.toDouble())
        }
    }

    if (state.config != null && state.theme != null && state.accentColor != null) {
        CompositionLocalProvider(LocalAnimation provides state.config.animations) {
            (AppTheme(
                themeColors = state.theme.colors,
                accentColor = state.accentColor
            )) {
                ContentWithOverlays {
                    Box(
                        modifier = modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().background(Theme[colors][backgroundAlt])
                        ) {
                            AppTopBar(
                                canGoBack = navStack.currentRecord != navStack.rootRecord,
                                canGoNext = navStack.currentRecord != navStack.topRecord,
                                onNavigateBack = { navigator.backward() },
                                onNavigateNext = { navigator.forward() }
                            )
                            Row {
                                AppMenu(
                                    currentBackStackEntry = navStack.currentRecord?.screen as? Screen,
                                    isDrawerOpen = state.config.menuDrawerOpen,
                                    onToggleDrawerClick = { eventSink(AppEvent.ToggleDrawer) },
                                    onMenuButtonClick = { navigator.goTo(it) }
                                )
                                AppContent(navStack, navigator)
                            }
                        }
                        AppSnackbarHost(
                            message = state.snackbarMessage,
                            onDismiss = { eventSink(AppEvent.DismissSnackbar) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun AppContent(
    navStack: NavStack<out NavStack.Record>,
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
                navStack = navStack
            )
        }
    }
}
