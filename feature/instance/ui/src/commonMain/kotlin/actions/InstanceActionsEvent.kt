package app.vercors.feature.instance.ui.actions

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.ui.copy.CopyInstanceResult
import app.vercors.feature.instance.ui.create.CreateInstanceResult
import app.vercors.feature.instance.ui.export.ExportInstanceResult
import app.vercors.shared.ui.Event

sealed interface InstanceActionsEvent : Event {
    data object OnCreateInstanceClick : InstanceActionsEvent
    data class OnCreateInstanceResult(val result: CreateInstanceResult) : InstanceActionsEvent
    data class OnInstanceClick(val instanceId: InstanceId) : InstanceActionsEvent
    data class OnInstanceLaunchClick(val instanceId: InstanceId) : InstanceActionsEvent
    data class OnInstanceStopClick(val instanceId: InstanceId) : InstanceActionsEvent
    data class OnInstanceKillClick(val instanceId: InstanceId) : InstanceActionsEvent
    data class OnOpenInstanceFolderClick(val instanceId: InstanceId) : InstanceActionsEvent
    data class OnOpenProjectFromInstanceClick(
        val instanceId: InstanceId,
        val platformId: String,
        val projectId: String
    ) : InstanceActionsEvent

    data class OnDeleteInstanceClick(val instanceId: InstanceId) : InstanceActionsEvent
    data class OnConfirmDeleteInstance(val result: Boolean) : InstanceActionsEvent
    data class OnCopyInstanceClick(val instanceId: InstanceId, val name: String) : InstanceActionsEvent
    data class OnCopyInstanceResult(val result: CopyInstanceResult) : InstanceActionsEvent
    data class OnExportInstanceClick(val instanceId: InstanceId, val name: String) : InstanceActionsEvent
    data class OnExportInstanceResult(val result: ExportInstanceResult) : InstanceActionsEvent
}
