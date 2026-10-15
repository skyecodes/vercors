package app.vercors.feature.instance.ui.details.header

import androidx.compose.runtime.Immutable
import app.vercors.feature.instance.ui.copy.CopyInstanceDialog
import app.vercors.feature.instance.ui.export.ExportInstanceDialog
import app.vercors.feature.instance.ui.options.copy.CopyOptionsDialog
import app.vercors.feature.instance.ui.options.savedefault.SaveDefaultOptionsDialog

@Immutable
data class InstanceHeaderState(
    val promptDeleteConfirmation: Boolean,
    val copyInstanceDialog: CopyInstanceDialog?,
    val exportInstanceDialog: ExportInstanceDialog?,
    val copyOptionsDialog: CopyOptionsDialog?,
    val saveDefaultOptionsDialog: SaveDefaultOptionsDialog?,
)
