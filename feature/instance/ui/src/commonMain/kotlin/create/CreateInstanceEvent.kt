package app.vercors.feature.instance.ui.create

import app.vercors.feature.game.ui.GameVersionView
import app.vercors.shared.ui.Event

sealed interface CreateInstanceEvent : Event {
    data class OnTabClick(val tab: CreateInstanceTab) : CreateInstanceEvent
    data class OnInstanceNameChange(val name: String) : CreateInstanceEvent
    data class OnGameVersionChange(val version: GameVersionView?) : CreateInstanceEvent
    data class OnGameVersionSearchChange(val search: String) : CreateInstanceEvent
    data object OnShowSnapshotClick : CreateInstanceEvent
    data object OnCreateInstanceClick : CreateInstanceEvent
}
