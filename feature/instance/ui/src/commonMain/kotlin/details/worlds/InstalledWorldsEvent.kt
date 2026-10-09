package app.vercors.feature.instance.ui.details.worlds

import app.vercors.shared.ui.Event

sealed interface InstalledWorldsEvent : Event {
    data class OnRemoveClick(val folderName: String) : InstalledWorldsEvent
    data class OnConfirmRemove(val result: Boolean) : InstalledWorldsEvent
    data class OnImportClick(val sourcePath: String) : InstalledWorldsEvent
}
