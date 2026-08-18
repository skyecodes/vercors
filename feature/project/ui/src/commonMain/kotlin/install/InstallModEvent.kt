package app.vercors.feature.project.ui.install

import app.vercors.shared.ui.Event

sealed interface InstallModEvent : Event {
    data class OnInstanceSelected(val instanceId: String?) : InstallModEvent
    data class OnFileSelected(val fileId: String?) : InstallModEvent
    data object OnConfirmClick : InstallModEvent
    data object OnCancelClick : InstallModEvent
}
