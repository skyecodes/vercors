package app.vercors.feature.home.ui

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.ui.copy.CopyInstanceResult
import app.vercors.feature.instance.ui.create.CreateInstanceResult
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.ui.BriefProjectView
import app.vercors.feature.project.ui.install.InstallModpackResult
import app.vercors.feature.project.ui.install.InstallProjectResult
import app.vercors.shared.ui.Event

sealed interface HomeEvent : Event {
    data object OnCreateInstanceClick : HomeEvent
    data class OnCreateInstanceResult(val result: CreateInstanceResult) : HomeEvent
    data class OnProjectClick(val projectId: ProjectId) : HomeEvent
    data class OnInstanceClick(val instanceId: InstanceId) : HomeEvent
    data class OnInstanceLaunchClick(val instanceId: InstanceId) : HomeEvent
    data class OnInstanceStopClick(val instanceId: InstanceId) : HomeEvent
    data class OnInstanceKillClick(val instanceId: InstanceId) : HomeEvent
    data class OnOpenInstanceFolderClick(val instanceId: InstanceId) : HomeEvent
    data class OnOpenProjectFromInstanceClick(
        val instanceId: InstanceId,
        val platformId: String,
        val projectId: String
    ) : HomeEvent

    data class OnDeleteInstanceClick(val instanceId: InstanceId) : HomeEvent
    data class OnConfirmDeleteInstance(val result: Boolean) : HomeEvent
    data class OnCopyInstanceClick(val instanceId: InstanceId, val name: String) : HomeEvent
    data class OnCopyInstanceResult(val result: CopyInstanceResult) : HomeEvent
    data class OnInstallProjectClick(val project: BriefProjectView) : HomeEvent
    data class OnInstallResult(val result: InstallProjectResult) : HomeEvent
    data class OnInstallModpackResult(val result: InstallModpackResult) : HomeEvent
}
