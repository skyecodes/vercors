package app.vercors.feature.config.ui.datadirectory

import app.vercors.shared.ui.Event

sealed interface ChangeDataDirectoryEvent : Event {
    data class OnPathChange(val value: String) : ChangeDataDirectoryEvent
    data object OnResetClick : ChangeDataDirectoryEvent
    data object OnConfirmClick : ChangeDataDirectoryEvent
    data object OnCancelClick : ChangeDataDirectoryEvent
}
