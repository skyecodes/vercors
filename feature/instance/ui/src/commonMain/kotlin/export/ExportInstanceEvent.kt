package app.vercors.feature.instance.ui.export

import app.vercors.feature.instance.domain.model.export.InstanceExportFormat
import app.vercors.shared.ui.Event

sealed interface ExportInstanceEvent : Event {
    data class OnFormatSelected(val format: InstanceExportFormat) : ExportInstanceEvent
    data class OnAdvancedOptionsToggle(val checked: Boolean) : ExportInstanceEvent
    data class OnEntryToggle(val name: String, val checked: Boolean) : ExportInstanceEvent
    data class OnReferenceKnownFilesToggle(val checked: Boolean) : ExportInstanceEvent
    data class OnVersionIdChange(val versionId: String) : ExportInstanceEvent
    data class OnSummaryChange(val summary: String) : ExportInstanceEvent
    data class OnTargetSelected(val path: String) : ExportInstanceEvent
    data object OnCancelClick : ExportInstanceEvent
}
