package app.vercors.feature.instance.ui

import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.delete
import app.vercors.shared.ui.overlay.ConfirmDialog
import app.vercors.shared.ui.overlay.ConfirmDialogButton
import app.vercors.shared.ui.overlay.ConfirmDialogButtonStyle
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Trash

fun deleteInstanceConfirmDialog(): ConfirmDialog = ConfirmDialog(
    messageResource = InstanceRes.string.deleteInstanceConfirmation,
    actionButton = ConfirmDialogButton(
        textResource = SharedRes.string.delete,
        icon = Lucide.Trash,
        style = ConfirmDialogButtonStyle.Destructive,
    ),
)

fun removeContentConfirmDialog(name: String): ConfirmDialog = ConfirmDialog(
    messageResource = InstanceRes.string.removeContentConfirmation,
    messageArgs = listOf(name),
    actionButton = ConfirmDialogButton(
        textResource = InstanceRes.string.remove,
        icon = Lucide.Trash,
        style = ConfirmDialogButtonStyle.Destructive,
    ),
)
