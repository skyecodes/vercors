package app.vercors.launcher.gui

import app.vercors.launcher.gui.instance.list.InstanceListRoute
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed

class AppContainer : Container<AppState, AppIntent, AppAction> {
    override val store = store(initial = AppState()) {
        configure {
            debuggable = true
            name = "AppContainer"
        }

        reduce { intent ->
            when (intent) {
                AppIntent.Back -> updateState { copy(backStack = backStack.dropLast(1)) }
                is AppIntent.Navigate -> updateState { copy(backStack = backStack + intent.route) }
            }
        }

        whileSubscribed {

        }
    }
}

data class AppState(
    val backStack: List<Any> = listOf(InstanceListRoute),
) : MVIState {
    val currentBackStackEntry: Any? = backStack.lastOrNull()
}

sealed interface AppIntent : MVIIntent {
    data object Back : AppIntent
    data class Navigate(val route: Any) : AppIntent
}

sealed interface AppAction : MVIAction
