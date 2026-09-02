package app.vercors.feature.instance.ui.copy

import androidx.compose.runtime.Immutable
import app.vercors.shared.ui.Event

@Immutable
sealed interface CopyInstanceEvent : Event {
    data class OnInstanceNameChange(val name: String) : CopyInstanceEvent
    data object OnCopyClick : CopyInstanceEvent
    data object OnCancelClick : CopyInstanceEvent
}
