package app.vercors.feature.instance.ui.copy

import app.vercors.shared.ui.Event

sealed interface CopyInstanceEvent : Event {
    data class OnInstanceNameChange(val name: String) : CopyInstanceEvent
    data object OnCopyClick : CopyInstanceEvent
    data object OnCancelClick : CopyInstanceEvent
}
