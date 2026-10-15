package app.vercors.feature.instance.ui.details.options

import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.shared.ui.Event

sealed interface InstanceOptionsEvent : Event {
    data class OnInstanceEditChange(val update: (InstanceEditState) -> InstanceEditState) : InstanceOptionsEvent
    data class OnLoaderChange(val loader: ModLoaderType?) : InstanceOptionsEvent
    data class OnLoaderVersionChange(val version: String) : InstanceOptionsEvent
    data class OnJavaOverrideChange(val installationId: String?) : InstanceOptionsEvent
    data class OnCheckForUpdatesToggle(val checked: Boolean) : InstanceOptionsEvent
    data object OnCheckForUpdatesReset : InstanceOptionsEvent
    data object OnCheckForUpdatesClick : InstanceOptionsEvent
    data object OnIdentifyUnknownFilesToggle : InstanceOptionsEvent
    data object OnSaveClick : InstanceOptionsEvent
    data object OnCancelEditClick : InstanceOptionsEvent
}
