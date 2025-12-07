package app.vercors.launcher.gui

import app.vercors.launcher.gui.config.model.GuiConfiguration
import app.vercors.launcher.gui.config.usecase.ObserveGuiConfigurationUseCase
import app.vercors.launcher.gui.config.usecase.UpdateGuiConfigurationUseCase
import app.vercors.launcher.gui.home.HomeRoute
import app.vercors.shared.domain.getOrNull
import app.vercors.shared.ui.configureWithLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.logging.logt
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed

private val logger = KotlinLogging.logger {}

class AppContainer(
    private val observeGuiConfiguration: ObserveGuiConfigurationUseCase,
    private val updateGuiConfiguration: UpdateGuiConfigurationUseCase,
) : Container<AppState, AppIntent, AppAction> {
    override val store = store(initial = AppState()) {
        configureWithLogger(logger) {
            debuggable = true
            name = "AppContainer"
        }

        reduce { intent ->
            logt { "Intent: $intent" }
            when (intent) {
                AppIntent.Back -> updateState {
                    copy(backStack = backStack.dropLast(1), frontStack = frontStack + backStack.last())
                }

                AppIntent.Next -> updateState {
                    copy(backStack = backStack + frontStack.last(), frontStack = frontStack.dropLast(1))
                }

                is AppIntent.Navigate -> updateState {
                    if (intent.route != currentBackStackEntry)
                        copy(backStack = backStack + intent.route, frontStack = emptyList())
                    else
                        this
                }

                AppIntent.ToggleDrawer -> updateGuiConfiguration { it.copy(menuDrawerOpen = !it.menuDrawerOpen) }
            }
        }

        whileSubscribed {
            observeGuiConfiguration().collect {
                updateState { copy(guiConfig = it.getOrNull()) }
            }
        }
    }
}

data class AppState(
    val backStack: List<Any> = listOf(HomeRoute),
    val frontStack: List<Any> = emptyList(),
    val guiConfig: GuiConfiguration? = null,
) : MVIState {
    val currentBackStackEntry: Any? = backStack.lastOrNull()
    val canGoBack: Boolean = backStack.size > 1
    val canGoNext: Boolean = frontStack.isNotEmpty()
}

sealed interface AppIntent : MVIIntent {
    data object Back : AppIntent
    data object Next : AppIntent
    data class Navigate(val route: Any) : AppIntent
    data object ToggleDrawer : AppIntent
}

sealed interface AppAction : MVIAction
