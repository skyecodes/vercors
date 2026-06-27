package app.vercors.feature.instance.ui.details

import androidx.compose.runtime.Immutable
import app.vercors.shared.ui.Event

@Immutable
sealed interface InstanceDetailsEvent : Event {
    data object Launch : InstanceDetailsEvent
    data object Cancel : InstanceDetailsEvent
    data object Stop : InstanceDetailsEvent
    data object Kill : InstanceDetailsEvent
    data object Delete : InstanceDetailsEvent
    data class ConfirmDelete(val result: Boolean) : InstanceDetailsEvent
    data class SelectTab(val tab: InstanceDetailsTab) : InstanceDetailsEvent
    data class UpdateInstanceName(val name: String) : InstanceDetailsEvent
    data class UpdateInstanceGameVersion(val gameVersion: String) : InstanceDetailsEvent
}
