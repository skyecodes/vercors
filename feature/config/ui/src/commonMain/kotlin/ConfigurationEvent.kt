package app.vercors.feature.config.ui

import app.vercors.shared.ui.Event

sealed interface ConfigurationEvent : Event {
    data class OnTabClick(val tab: ConfigurationTab) : ConfigurationEvent
}
