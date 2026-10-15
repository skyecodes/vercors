package app.vercors.feature.instance.ui.details.update

import app.vercors.shared.ui.Event

sealed interface InstanceUpdateEvent : Event {
    data object OnUpdateModpackClick : InstanceUpdateEvent
    data class OnConfirmModpackUpdate(val result: Boolean) : InstanceUpdateEvent
    data class OnUpdateFromFileSelected(val sourcePath: String) : InstanceUpdateEvent
    data class OnConfirmLocalPackUpdate(val result: Boolean) : InstanceUpdateEvent
}
