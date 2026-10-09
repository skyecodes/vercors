package app.vercors.feature.instance.ui.details

import app.vercors.feature.instance.domain.model.InstanceFolder
import app.vercors.feature.instance.ui.copy.CopyInstanceResult
import app.vercors.feature.launcher.domain.model.QuickPlayTarget
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.shared.ui.Event

sealed interface InstanceDetailsEvent : Event {
    data object OnLaunchClick : InstanceDetailsEvent
    data class OnQuickJoinClick(val target: QuickPlayTarget) : InstanceDetailsEvent
    data object OnCancelClick : InstanceDetailsEvent
    data object OnCancelSetupClick : InstanceDetailsEvent
    data object OnStopClick : InstanceDetailsEvent
    data object OnKillClick : InstanceDetailsEvent
    data object OnDeleteClick : InstanceDetailsEvent
    data class OnConfirmDelete(val result: Boolean) : InstanceDetailsEvent
    data object OnCopyClick : InstanceDetailsEvent
    data class OnCopyInstanceResult(val result: CopyInstanceResult) : InstanceDetailsEvent
    data class OnTabClick(val tab: InstanceDetailsTab) : InstanceDetailsEvent
    data object OnInstallJavaConfirm : InstanceDetailsEvent
    data object OnInstallJavaDismiss : InstanceDetailsEvent
    data class OnOpenProjectClick(val platformId: String, val projectId: String) : InstanceDetailsEvent
    data class OnAddContentClick(val type: ProjectType) : InstanceDetailsEvent
    data class OnIconSelected(val path: String) : InstanceDetailsEvent
    data object OnRemoveIconClick : InstanceDetailsEvent
    data object OnRestoreIconClick : InstanceDetailsEvent
    data class OnOpenInstanceFolderClick(val folder: InstanceFolder) : InstanceDetailsEvent
    data object OnFinishInstallationClick : InstanceDetailsEvent
    data object OnFinishInstallationDismiss : InstanceDetailsEvent
    data object OnResumeInstallationClick : InstanceDetailsEvent
    data object OnLoginDismiss : InstanceDetailsEvent
    data object OnJavaInstallProgressDismiss : InstanceDetailsEvent
}
