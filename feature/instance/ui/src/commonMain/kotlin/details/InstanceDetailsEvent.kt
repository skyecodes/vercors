package app.vercors.feature.instance.ui.details

import androidx.compose.runtime.Immutable
import app.vercors.feature.game.domain.model.ModLoaderType
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
    data class UpdateInstance(val update: (InstanceEditState) -> InstanceEditState) : InstanceDetailsEvent
    data class OnLoaderChange(val loader: ModLoaderType?) : InstanceDetailsEvent
    data class OnLoaderVersionChange(val version: String) : InstanceDetailsEvent
    data class OnJavaOverrideChange(val installationId: String?) : InstanceDetailsEvent
    data object OnInstallJavaConfirm : InstanceDetailsEvent
    data object OnInstallJavaDismiss : InstanceDetailsEvent
    data object SaveInstance : InstanceDetailsEvent
    data object CancelEdit : InstanceDetailsEvent
    data class OnModEnabledChange(val fileName: String, val enabled: Boolean) : InstanceDetailsEvent
    data class OnResourcePackEnabledChange(val fileName: String, val enabled: Boolean) : InstanceDetailsEvent
    data class OnShaderPackEnabledChange(val fileName: String, val enabled: Boolean) : InstanceDetailsEvent
    data class OnRemoveClick(val kind: RemovalKind, val fileName: String, val title: String) : InstanceDetailsEvent
    data class OnConfirmRemove(val result: Boolean) : InstanceDetailsEvent
    data class OnOpenProjectClick(val platformId: String, val projectId: String) : InstanceDetailsEvent
    data class OnAddContentClick(val type: ProjectType) : InstanceDetailsEvent
    data class OnIconSelected(val path: String) : InstanceDetailsEvent
    data object OnRemoveIconClick : InstanceDetailsEvent
    data class OnImportContentClick(val kind: RemovalKind, val path: String) : InstanceDetailsEvent
    data class OnOpenInstanceFolderClick(val folder: InstanceFolder) : InstanceDetailsEvent
    data object OnFinishInstallationClick : InstanceDetailsEvent
    data object OnFinishInstallationDismiss : InstanceDetailsEvent
    data object OnResumeInstallationClick : InstanceDetailsEvent
    data object CancelLogin : InstanceDetailsEvent
    data object OnJavaInstallDialogDismiss : InstanceDetailsEvent
    data class OnLogSearchQueryChange(val query: String) : InstanceDetailsEvent
    data class OnLogLevelFilterToggle(val level: InstanceLogEventView.Level) : InstanceDetailsEvent
    data object OnWrapLogLinesToggle : InstanceDetailsEvent
}
