package app.vercors.launcher.gui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.LocalContextMenuRepresentation
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.animation.LocalAnimation
import app.vercors.core.ui.component.AppContextMenuRepresentation
import app.vercors.core.ui.component.AppIconTextButton
import app.vercors.core.ui.layout.AppConfirmDialog
import app.vercors.core.ui.theme.*
import app.vercors.feature.account.ui.list.AccountListScreen
import app.vercors.feature.config.domain.model.Configuration
import app.vercors.feature.config.domain.model.StartupScreen
import app.vercors.feature.config.ui.ConfigurationScreen
import app.vercors.feature.home.ui.HomeScreen
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.ui.details.InstanceDetailsScreen
import app.vercors.feature.instance.ui.list.InstanceListScreen
import app.vercors.feature.project.ui.list.ProjectListScreen
import app.vercors.feature.theme.domain.model.ThemeDefinition
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

    AppAotTrainingEffect(navigator)
    AppTaskbarProgressEffect(state.installTaskbarProgress)
    AppStartupNavigationEffect(state, navigator)

    if (state.config != null && state.theme != null && state.accentColor != null) {
        AppMainContent(
            state = state,
            config = state.config,
            theme = state.theme,
            accentColor = state.accentColor,
            navStack = navStack,
            navigator = navigator,
            eventSink = eventSink,
            modifier = modifier,
        )
    }
}

@Composable
private fun AppAotTrainingEffect(navigator: Navigator) {
    val nucleusApplicationScope = LocalNucleusApplicationScope.current
    if (nucleusApplicationScope.isAotTraining) {
        nucleusApplicationScope.runAotTraining(navigator, LocalAppGraph.current)
    }
}

@Composable
private fun AppTaskbarProgressEffect(installTaskbarProgress: InstallTaskbarProgress) {
    val nucleusWindow = LocalNucleusWindow.current
    LaunchedEffect(installTaskbarProgress) {
        when (installTaskbarProgress) {
            InstallTaskbarProgress.None -> nucleusWindow.hideTaskbarProgress()
            InstallTaskbarProgress.Indeterminate -> nucleusWindow.showTaskbarIndeterminate()
            is InstallTaskbarProgress.Fraction -> nucleusWindow.showTaskbarProgress(installTaskbarProgress.value.toDouble())
        }
    }
}

@Composable
private fun AppStartupNavigationEffect(state: AppState, navigator: Navigator) {
    var startupNavigationDone by remember { mutableStateOf(false) }
    LaunchedEffect(state.config, state.instancesLoaded, state.lastPlayedInstanceId) {
        if (startupNavigationDone) return@LaunchedEffect
        val cfg = state.config ?: return@LaunchedEffect
        if (cfg.startupScreen == StartupScreen.LastPlayedInstance && !state.instancesLoaded) return@LaunchedEffect
        startupNavigationDone = true
        cfg.startupScreen.toStartupDestination(state.lastPlayedInstanceId)?.let { navigator.resetRoot(it) }
    }
}

private fun StartupScreen.toStartupDestination(lastPlayedInstanceId: InstanceId?): Screen? = when (this) {
    StartupScreen.Home -> null
    StartupScreen.Instances -> InstanceListScreen
    StartupScreen.Content -> ProjectListScreen()
    StartupScreen.Settings -> ConfigurationScreen
    StartupScreen.Accounts -> AccountListScreen
    StartupScreen.LastPlayedInstance -> lastPlayedInstanceId?.let { InstanceDetailsScreen(it) }
}

@Composable
private fun AppMainContent(
    state: AppState,
    config: Configuration,
    theme: ThemeDefinition,
    accentColor: Long,
    navStack: NavStack<out NavStack.Record>,
    navigator: Navigator,
    eventSink: EventSink<AppEvent>,
    modifier: Modifier,
) {
    val density = LocalDensity.current
    val scaledDensity = remember(density, config.uiScalePercent) {
        Density(density.density * (config.uiScalePercent / 100f), density.fontScale)
    }
    CompositionLocalProvider(
        LocalDensity provides scaledDensity,
        LocalAnimation provides config.animations,
        LocalContextMenuRepresentation provides AppContextMenuRepresentation,
    ) {
        (AppTheme(
            themeColors = theme.colors,
            accentColor = accentColor
        )) {
            Box(
                modifier = modifier.fillMaxSize()
            ) {
                ContentWithOverlays(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.fillMaxSize().background(Theme[colors][backgroundAlt])
                            .pointerInput(navigator) {
                                awaitPointerEventScope {
                                    manageNavigationButtons(navigator)
                                }
                            }
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
                                isDrawerOpen = config.menuDrawerOpen,
                                onToggleDrawerClick = { eventSink(AppEvent.ToggleDrawer) },
                                onMenuButtonClick = { navigator.goTo(it) }
                            )
                            AppContent(navStack, navigator)
                        }
                    }
                    AppQuitConfirmationDialog()
                }
                AppSnackbarHost(
                    message = state.snackbarMessage,
                    onDismiss = { eventSink(AppEvent.DismissSnackbar) },
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private suspend fun AwaitPointerEventScope.manageNavigationButtons(navigator: Navigator) {
    while (true) {
        val event = awaitPointerEvent(PointerEventPass.Initial)
        if (event.type != PointerEventType.Press) continue
        when (event.button) {
            PointerButton.Back -> navigator.backward()
            PointerButton.Forward -> navigator.forward()
            else -> continue
        }
        event.changes.forEach { it.consume() }
    }
}

@Composable
private fun AppQuitConfirmationDialog() {
    val quitConfirmation = LocalQuitConfirmation.current
    if (!quitConfirmation.requested) return
    val nucleusApplicationScope = LocalNucleusApplicationScope.current
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
