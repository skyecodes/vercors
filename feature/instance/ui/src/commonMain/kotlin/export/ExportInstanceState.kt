package app.vercors.feature.instance.ui.export

import androidx.compose.runtime.Immutable
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.export.ExportableEntry
import app.vercors.feature.instance.domain.model.export.InstanceExportFormat

@Immutable
data class ExportInstanceState(
    val suggestedFileName: String,
    val format: InstanceExportFormat,
    val entries: List<ExportEntryView>?,
    val showAdvancedOptions: Boolean,
    val referenceKnownFiles: Boolean,
    val versionId: String,
    val summary: String,
    val exportProgress: Float?,
) {
    val isExporting: Boolean = exportProgress != null
    val canExport: Boolean = !isExporting && entries != null &&
            (format != InstanceExportFormat.Modrinth || versionId.isNotBlank())
}

@Immutable
data class ExportEntryView(
    val name: String,
    val isDirectory: Boolean,
    val checked: Boolean,
    val includedByDefault: Boolean,
)

data class ExportInstanceTarget(val id: InstanceId, val name: String)

private val FILE_NAME_FORBIDDEN_CHARS = Regex("""[<>:"/\\|?*\x00-\x1F]""")

// Prism names an imported instance after the archive's file name, ignoring instance.cfg, so keep the display name
fun ExportInstanceTarget.toSuggestedFileName(): String =
    name.replace(FILE_NAME_FORBIDDEN_CHARS, "_").trim().trimEnd('.').ifEmpty { id }

fun ExportableEntry.toView(): ExportEntryView = ExportEntryView(
    name = name,
    isDirectory = isDirectory,
    checked = includedByDefault,
    includedByDefault = includedByDefault,
)
