package app.vercors.launcher.gui

import androidx.compose.runtime.Immutable
import app.vercors.feature.config.gui.domain.model.GuiConfiguration

@Immutable
data class AppState(
    val guiConfig: GuiConfiguration?,
    val isMinimized: Boolean,
    val onWindowEvent: (AppWindowEvent) -> Unit
)
