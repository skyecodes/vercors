package app.vercors.feature.project.ui.install

import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.shared.ui.Event

sealed interface InstallProjectEvent : Event {
    data class OnInstanceSelected(val instanceId: String?) : InstallProjectEvent
    data class OnFileSelected(val fileId: String?) : InstallProjectEvent
    data class OnDependencyToggled(val projectId: ProjectId, val checked: Boolean) : InstallProjectEvent
    data class OnCopyUrlClick(val url: String) : InstallProjectEvent
    data object OnConfirmClick : InstallProjectEvent
    data object OnCancelClick : InstallProjectEvent
    data object OnCloseClick : InstallProjectEvent
    data object OnGoToInstanceClick : InstallProjectEvent
}
