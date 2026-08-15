package app.vercors.launcher.gui

import androidx.compose.runtime.Immutable
import app.vercors.feature.config.domain.model.Configuration
import app.vercors.feature.theme.domain.model.ThemeDefinition

@Immutable
data class AppState(
    val config: Configuration?,
    val theme: ThemeDefinition?,
    val accentColor: Long?,
)
