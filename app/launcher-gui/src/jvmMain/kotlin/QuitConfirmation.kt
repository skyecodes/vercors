package app.vercors.launcher.gui

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

internal data class QuitConfirmationState(val requested: Boolean, val onDismiss: () -> Unit)

internal val LocalQuitConfirmation: ProvidableCompositionLocal<QuitConfirmationState> =
    staticCompositionLocalOf { QuitConfirmationState(requested = false, onDismiss = {}) }
