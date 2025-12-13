package app.vercors.launcher.gui.navigation

import androidx.compose.runtime.staticCompositionLocalOf

val LocalNavigator = staticCompositionLocalOf<Navigator> { error("No Navigator provided") }

interface Navigator {
    fun navigateTo(route: Route)
    fun navigateBack()
    fun navigateNext()
}
