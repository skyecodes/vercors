package app.vercors.feature.config.ui

import app.vercors.shared.ui.Event

sealed interface ConfigurationEvent : Event {
    @JvmInline
    value class SelectTheme(val themeId: String) : ConfigurationEvent

    @JvmInline
    value class SelectAccent(val accentId: String) : ConfigurationEvent

    data object ToggleAnimations : ConfigurationEvent
}
