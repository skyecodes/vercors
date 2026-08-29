package app.vercors.feature.instance.ui.details

import androidx.compose.runtime.Immutable
import app.vercors.feature.instance.domain.model.InstanceFolder
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.shared.ui.Event

@Immutable
sealed interface InstanceDetailsEvent : Event {
    data object Launch : InstanceDetailsEvent
    data object Cancel : InstanceDetailsEvent
    data object CancelSetup : InstanceDetailsEvent
    data object Stop : InstanceDetailsEvent
    data object Kill : InstanceDetailsEvent
    data object Delete : InstanceDetailsEvent
    data class ConfirmDelete(val result: Boolean) : InstanceDetailsEvent
    data class SelectTab(val tab: InstanceDetailsTab) : InstanceDetailsEvent
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
    data object CancelLogin : InstanceDetailsEvent
    data object OnJavaInstallDialogDismiss : InstanceDetailsEvent
}
