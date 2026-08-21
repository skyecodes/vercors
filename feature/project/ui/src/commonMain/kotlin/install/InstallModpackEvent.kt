package app.vercors.feature.project.ui.install

import app.vercors.shared.ui.Event

sealed interface InstallModpackEvent : Event {
    data class OnFileSelected(val fileId: String?) : InstallModpackEvent
    data class OnInstanceNameChange(val name: String) : InstallModpackEvent
    data object OnConfirmClick : InstallModpackEvent
    data object OnCancelClick : InstallModpackEvent
}
