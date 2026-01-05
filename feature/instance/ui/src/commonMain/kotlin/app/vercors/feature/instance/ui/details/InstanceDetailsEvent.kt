package app.vercors.feature.instance.ui.details

import app.vercors.shared.ui.Event

sealed interface InstanceDetailsEvent : Event {
    data object Launch : InstanceDetailsEvent
    data object Delete : InstanceDetailsEvent
}
