package app.vercors.feature.config.ui.appearance

import app.vercors.shared.ui.Event

sealed interface AppearanceConfigurationEvent : Event {
    data class OnThemeChange(val themeId: String) : AppearanceConfigurationEvent
    data class OnAccentChange(val accentId: String) : AppearanceConfigurationEvent
    data object OnAnimationsToggle : AppearanceConfigurationEvent
    data object OnHorizontalTabsToggle : AppearanceConfigurationEvent
    data class OnUiScalePercentChange(val value: Int) : AppearanceConfigurationEvent
}
