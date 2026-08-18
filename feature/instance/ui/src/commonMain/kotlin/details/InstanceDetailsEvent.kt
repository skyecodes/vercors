package app.vercors.feature.instance.ui.details

import androidx.compose.runtime.Immutable
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.shared.ui.Event

@Immutable
sealed interface InstanceDetailsEvent : Event {
    data object Launch : InstanceDetailsEvent
    data object Cancel : InstanceDetailsEvent
    data object Stop : InstanceDetailsEvent
    data object Kill : InstanceDetailsEvent
    data object Delete : InstanceDetailsEvent
    data class ConfirmDelete(val result: Boolean) : InstanceDetailsEvent
    data class SelectTab(val tab: InstanceDetailsTab) : InstanceDetailsEvent
    data class UpdateInstance(val update: (InstanceEditState) -> InstanceEditState) : InstanceDetailsEvent
    data class OnLoaderChange(val loader: ModLoaderType?) : InstanceDetailsEvent
    data class OnLoaderVersionChange(val version: String) : InstanceDetailsEvent
    data object SaveInstance : InstanceDetailsEvent
    data object CancelEdit : InstanceDetailsEvent
    data class OnModEnabledChange(val fileName: String, val enabled: Boolean) : InstanceDetailsEvent
    data class OnResourcePackEnabledChange(val fileName: String, val enabled: Boolean) : InstanceDetailsEvent
    data class OnShaderPackEnabledChange(val fileName: String, val enabled: Boolean) : InstanceDetailsEvent
}
