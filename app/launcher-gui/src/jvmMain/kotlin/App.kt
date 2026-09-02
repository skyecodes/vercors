package app.vercors.launcher.gui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.LocalContextMenuRepresentation
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.animation.LocalAnimation
import app.vercors.core.ui.component.AppContextMenuRepresentation
import app.vercors.core.ui.component.AppIconTextButton
import app.vercors.core.ui.layout.AppConfirmDialog
import app.vercors.core.ui.theme.*
import app.vercors.feature.home.ui.HomeScreen
import app.vercors.shared.ui.*
import com.composables.icons.lucide.LogOut
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.composeunstyled.theme.Theme
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.navstack.rememberSaveableNavStack
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.navigation.NavStack
import com.slack.circuit.sharedelements.SharedElementTransitionLayout
import dev.nucleusframework.application.LocalNucleusApplicationScope
import dev.nucleusframework.application.LocalNucleusWindow
import dev.nucleusframework.taskbarprogress.tao.hideTaskbarProgress
import dev.nucleusframework.taskbarprogress.tao.showTaskbarIndeterminate
import dev.nucleusframework.taskbarprogress.tao.showTaskbarProgress
import dev.zacsweers.metro.AppScope
import org.jetbrains.compose.resources.stringResource

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
    val nucleusApplicationScope = LocalNucleusApplicationScope.current
    val quitConfirmation = LocalQuitConfirmation.current

    if (nucleusApplicationScope.isAotTraining) {
        nucleusApplicationScope.runAotTraining(navigator, LocalAppGraph.current)
    }

    LaunchedEffect(state.installTaskbarProgress) {
        when (val progress = state.installTaskbarProgress) {
            InstallTaskbarProgress.None -> nucleusWindow.hideTaskbarProgress()
            InstallTaskbarProgress.Indeterminate -> nucleusWindow.showTaskbarIndeterminate()
            is InstallTaskbarProgress.Fraction -> nucleusWindow.showTaskbarProgress(progress.value.toDouble())
        }
    }

    if (state.config != null && state.theme != null && state.accentColor != null) {
        val density = LocalDensity.current
        val scaledDensity = remember(density, state.config.uiScalePercent) {
            Density(density.density * (state.config.uiScalePercent / 100f), density.fontScale)
        }
        CompositionLocalProvider(
            LocalDensity provides scaledDensity,
            LocalAnimation provides state.config.animations,
            LocalContextMenuRepresentation provides AppContextMenuRepresentation,
        ) {
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
                                onNavigateNext = { navigator.forward() },
                                appName = state.appName
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
                        if (quitConfirmation.requested) {
                            AppConfirmDialog(
                                confirmText = stringResource(Res.string.quitRunningInstancesConfirmation),
                                confirmButton = {
                                    AppIconTextButton(
                                        onClick = quitConfirmation.onDismiss,
                                        text = stringResource(SharedRes.string.cancel),
                                        imageVector = Lucide.X,
                                    )
                                },
                                dismissButton = {
                                    AppIconTextButton(
                                        onClick = { nucleusApplicationScope.exitApplication() },
                                        text = stringResource(SharedRes.string.quit),
                                        imageVector = Lucide.LogOut,
                                        backgroundColor = Theme[colors][error],
                                        contentColor = Theme[colors][onError],
                                    )
                                },
                                onClickOutside = quitConfirmation.onDismiss,
                            )
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
