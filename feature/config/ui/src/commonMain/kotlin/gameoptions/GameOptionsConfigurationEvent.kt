package app.vercors.feature.config.ui.gameoptions

import app.vercors.shared.ui.Event

sealed interface GameOptionsConfigurationEvent : Event {
    data object OnAutoApplyToggle : GameOptionsConfigurationEvent
    data object OnClearClick : GameOptionsConfigurationEvent
    data class OnConfirmClear(val result: Boolean) : GameOptionsConfigurationEvent
}
