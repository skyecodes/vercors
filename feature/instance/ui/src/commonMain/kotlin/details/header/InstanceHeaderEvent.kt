package app.vercors.feature.instance.ui.details.header

import app.vercors.feature.instance.domain.model.InstanceFolder
import app.vercors.feature.instance.ui.copy.CopyInstanceResult
import app.vercors.feature.instance.ui.export.ExportInstanceResult
import app.vercors.feature.instance.ui.options.copy.CopyOptionsResult
import app.vercors.feature.instance.ui.options.savedefault.SaveDefaultOptionsResult
import app.vercors.shared.ui.Event

sealed interface InstanceHeaderEvent : Event {
    data object OnDeleteClick : InstanceHeaderEvent
    data class OnConfirmDelete(val result: Boolean) : InstanceHeaderEvent
    data class OnCopyClick(val name: String) : InstanceHeaderEvent
    data class OnCopyInstanceResult(val result: CopyInstanceResult) : InstanceHeaderEvent
    data class OnExportClick(val name: String) : InstanceHeaderEvent
    data class OnExportInstanceResult(val result: ExportInstanceResult) : InstanceHeaderEvent
    data class OnCopyOptionsClick(val gameVersion: String) : InstanceHeaderEvent
    data class OnCopyOptionsResult(val result: CopyOptionsResult) : InstanceHeaderEvent
    data object OnSaveDefaultOptionsClick : InstanceHeaderEvent
    data class OnSaveDefaultOptionsResult(val result: SaveDefaultOptionsResult) : InstanceHeaderEvent
    data class OnIconSelected(val path: String) : InstanceHeaderEvent
    data object OnRemoveIconClick : InstanceHeaderEvent
    data object OnRestoreIconClick : InstanceHeaderEvent
    data class OnOpenInstanceFolderClick(val folder: InstanceFolder) : InstanceHeaderEvent
}
