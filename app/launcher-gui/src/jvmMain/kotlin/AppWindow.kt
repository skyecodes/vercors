package app.vercors.launcher.gui

import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.window.WindowScope

sealed interface AppWindowEvent {
    data object Minimize : AppWindowEvent
    data object Maximize : AppWindowEvent
    data object Close : AppWindowEvent
}

@Stable
internal interface AppWindow : WindowScope {
    val isMaximized: Boolean
    fun onEvent(event: AppWindowEvent)
}

internal val LocalAppWindow = staticCompositionLocalOf<AppWindow> { error("AppWindow must be set") }
