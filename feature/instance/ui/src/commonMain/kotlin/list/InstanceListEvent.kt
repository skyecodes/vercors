package app.vercors.feature.instance.ui.list

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.ui.copy.CopyInstanceResult
import app.vercors.feature.instance.ui.create.CreateInstanceResult
import app.vercors.shared.ui.Event

sealed interface InstanceListEvent : Event {
    data object OnCreateInstanceClick : InstanceListEvent
    data class OnCreateInstanceResult(val result: CreateInstanceResult) : InstanceListEvent
    data class OnNameFilterChange(val name: String) : InstanceListEvent
    data class OnTypeFilterChange(val type: String) : InstanceListEvent
    data class OnLoaderFilterChange(val loader: String) : InstanceListEvent
    data class OnVersionFilterChange(val version: String) : InstanceListEvent
    data class OnInstanceClick(val instanceId: InstanceId) : InstanceListEvent
    data class OnInstanceLaunchClick(val instanceId: InstanceId) : InstanceListEvent
    data class OnInstanceStopClick(val instanceId: InstanceId) : InstanceListEvent
    data class OnInstanceKillClick(val instanceId: InstanceId) : InstanceListEvent
    data class OnOpenInstanceFolderClick(val instanceId: InstanceId) : InstanceListEvent
    data class OnOpenProjectFromInstanceClick(
        val instanceId: InstanceId,
        val platformId: String,
        val projectId: String
    ) : InstanceListEvent

    data class OnDeleteInstanceClick(val instanceId: InstanceId) : InstanceListEvent
    data class OnConfirmDeleteInstance(val result: Boolean) : InstanceListEvent
    data class OnCopyInstanceClick(val instanceId: InstanceId, val name: String) : InstanceListEvent
    data class OnCopyInstanceResult(val result: CopyInstanceResult) : InstanceListEvent
    data object OnResetFilterClick : InstanceListEvent
}
