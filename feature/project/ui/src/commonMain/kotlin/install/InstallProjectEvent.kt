package app.vercors.feature.project.ui.install

import app.vercors.shared.ui.Event

sealed interface InstallProjectEvent : Event {
    data class OnInstanceSelected(val instanceId: String?) : InstallProjectEvent
    data class OnFileSelected(val fileId: String?) : InstallProjectEvent
    data object OnConfirmClick : InstallProjectEvent
    data object OnCancelClick : InstallProjectEvent
}
