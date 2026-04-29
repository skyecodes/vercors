package app.vercors.launcher.gui

import androidx.compose.runtime.Immutable
import app.vercors.feature.config.domain.model.Configuration
import app.vercors.feature.theme.domain.model.AppThemeDefinition

@Immutable
data class AppState(
    val config: Configuration?,
    val isMinimized: Boolean,
    val onWindowEvent: (AppWindowEvent) -> Unit,
    val activeTheme: AppThemeDefinition?,
)
