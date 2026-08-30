package app.vercors.launcher.gui

import androidx.compose.runtime.Immutable
import app.vercors.feature.config.domain.model.Configuration
import app.vercors.feature.theme.domain.model.ThemeDefinition
import app.vercors.shared.ui.SnackbarMessage

@Immutable
data class AppState(
    val config: Configuration?,
    val theme: ThemeDefinition?,
    val accentColor: Long?,
    val snackbarMessage: SnackbarMessage?,
    val installTaskbarProgress: InstallTaskbarProgress,
    val appName: String
)

/** Aggregate taskbar-progress signal across every instance currently installing or launching. */
@Immutable
sealed interface InstallTaskbarProgress {
    data object None : InstallTaskbarProgress
    data object Indeterminate : InstallTaskbarProgress
    data class Fraction(val value: Float) : InstallTaskbarProgress
}
