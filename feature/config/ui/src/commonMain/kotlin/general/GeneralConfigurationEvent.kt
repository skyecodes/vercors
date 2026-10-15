package app.vercors.feature.config.ui.general

import app.vercors.feature.config.domain.model.HomeCategoryConfig
import app.vercors.feature.config.domain.model.StartupScreen
import app.vercors.shared.ui.Event

sealed interface GeneralConfigurationEvent : Event {
    data class OnPreferredPlatformChange(val value: String) : GeneralConfigurationEvent
    data class OnStartupScreenChange(val value: StartupScreen) : GeneralConfigurationEvent
    data object OnCheckForInstanceUpdatesToggle : GeneralConfigurationEvent
    data class OnHomeCategoriesChange(val value: List<HomeCategoryConfig>) : GeneralConfigurationEvent
}
