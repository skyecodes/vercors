package app.vercors.feature.instance.ui.actions

import androidx.compose.runtime.Immutable
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.ui.copy.CopyInstanceDialog
import app.vercors.feature.instance.ui.create.CreateInstanceResult
import app.vercors.feature.instance.ui.export.ExportInstanceDialog
import app.vercors.shared.ui.overlay.DialogOverlay

@Immutable
data class InstanceActionsState(
    val promptCreateInstance: Boolean,
    val createInstanceDialog: DialogOverlay<CreateInstanceResult>,
    val pendingDeleteInstanceId: InstanceId?,
    val copyInstanceDialog: CopyInstanceDialog?,
    val exportInstanceDialog: ExportInstanceDialog?,
)
