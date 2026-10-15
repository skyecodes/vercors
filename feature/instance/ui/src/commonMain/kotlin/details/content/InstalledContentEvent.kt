package app.vercors.feature.instance.ui.details.content

import app.vercors.shared.ui.Event

sealed interface InstalledContentEvent : Event {
    data class OnEnabledChange(val fileName: String, val enabled: Boolean) : InstalledContentEvent
    data class OnRemoveClick(val fileName: String, val title: String) : InstalledContentEvent
    data class OnConfirmRemove(val result: Boolean) : InstalledContentEvent
    data class OnImportClick(val sourcePath: String) : InstalledContentEvent
    data class OnUpdateClick(val fileName: String) : InstalledContentEvent
    data object OnUpdateAllClick : InstalledContentEvent
}
