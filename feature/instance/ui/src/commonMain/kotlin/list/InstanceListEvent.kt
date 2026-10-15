package app.vercors.feature.instance.ui.list

import app.vercors.shared.ui.Event

sealed interface InstanceListEvent : Event {
    data class OnNameFilterChange(val name: String) : InstanceListEvent
    data class OnTypeFilterChange(val type: String) : InstanceListEvent
    data class OnLoaderFilterChange(val loader: String) : InstanceListEvent
    data class OnVersionFilterChange(val version: String) : InstanceListEvent
    data object OnResetFilterClick : InstanceListEvent
}
