package app.vercors.launcher.gui

import androidx.compose.runtime.Immutable
import app.vercors.core.domain.orNull
import app.vercors.feature.config.gui.domain.model.GuiConfiguration
import app.vercors.feature.config.gui.domain.usecase.ObserveGuiConfigurationUseCase
import app.vercors.feature.config.gui.domain.usecase.UpdateGuiConfigurationUseCase
import app.vercors.feature.home.ui.HomeRoute
import app.vercors.shared.ui.Route
import app.vercors.shared.ui.configureWithLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed

private val logger = KotlinLogging.logger {}

class AppContainer(
    private val observeGuiConfiguration: ObserveGuiConfigurationUseCase,
    private val updateGuiConfiguration: UpdateGuiConfigurationUseCase,
) : Container<AppState, AppIntent, AppAction> {
    override val store = store(initial = AppState()) {
        configureWithLogger(logger)

        reduce { intent ->
            when (intent) {
                AppIntent.Back -> updateState {
                    copy(backStack = backStack.dropLast(1), nextStack = nextStack + backStack.last())
                }

                AppIntent.Next -> updateState {
                    copy(backStack = backStack + nextStack.last(), nextStack = nextStack.dropLast(1))
                }

                is AppIntent.Navigate -> updateState {
                    if (intent.route != currentBackStackEntry)
                        copy(backStack = backStack + intent.route, nextStack = emptyList())
                    else
                        this
                }

                AppIntent.ToggleDrawer -> updateGuiConfiguration { it.copy(menuDrawerOpen = !it.menuDrawerOpen) }
            }
        }

        whileSubscribed {
            observeGuiConfiguration().collect {
                updateState { copy(guiConfig = it.orNull()) }
            }
        }
    }
}

@Immutable
data class AppState(
    val backStack: List<Route> = listOf(HomeRoute),
    val nextStack: List<Route> = emptyList(),
    val guiConfig: GuiConfiguration? = null,
) : MVIState {
    val currentBackStackEntry: Route? = backStack.lastOrNull()
    val canGoBack: Boolean = backStack.size > 1
    val canGoNext: Boolean = nextStack.isNotEmpty()
}

sealed interface AppIntent : MVIIntent {
    data object Back : AppIntent
    data object Next : AppIntent
    data class Navigate(val route: Route) : AppIntent
    data object ToggleDrawer : AppIntent
}

sealed interface AppAction : MVIAction
