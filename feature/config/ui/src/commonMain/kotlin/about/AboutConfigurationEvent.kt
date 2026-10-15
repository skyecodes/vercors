package app.vercors.feature.config.ui.about

import app.vercors.feature.config.domain.model.UpdateChannel
import app.vercors.shared.ui.Event

sealed interface AboutConfigurationEvent : Event {
    data object OnCheckForUpdatesOnStartToggle : AboutConfigurationEvent
    data class OnUpdateChannelChange(val value: UpdateChannel) : AboutConfigurationEvent
    data object OnCheckForUpdatesClick : AboutConfigurationEvent
    data object OnUpdateClick : AboutConfigurationEvent
    data object OnRestartToUpdateClick : AboutConfigurationEvent
    data class OnConfirmRestartForUpdate(val result: Boolean) : AboutConfigurationEvent
}
