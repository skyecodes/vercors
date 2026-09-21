package app.vercors.feature.config.ui.datadirectory

import androidx.compose.runtime.Immutable
import app.vercors.shared.ui.Event

@Immutable
sealed interface ChangeDataDirectoryEvent : Event {
    data class OnPathChange(val value: String) : ChangeDataDirectoryEvent
    data object OnResetClick : ChangeDataDirectoryEvent
    data object OnConfirmClick : ChangeDataDirectoryEvent
    data object OnCancelClick : ChangeDataDirectoryEvent
}
