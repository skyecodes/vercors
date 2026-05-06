package app.vercors.feature.instance.ui.delete

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import app.vercors.core.ui.component.AppIconTextButton
import app.vercors.core.ui.layout.AppConfirmDialog
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.error
import app.vercors.core.ui.theme.onError
import app.vercors.feature.instance.ui.InstanceRes
import app.vercors.feature.instance.ui.deleteInstanceConfirmation
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.cancel
import app.vercors.shared.ui.delete
import app.vercors.shared.ui.overlay.DialogOverlay
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Trash
import com.composables.icons.lucide.X
import com.composeunstyled.theme.Theme
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.overlay.OverlayTransitionController
import org.jetbrains.compose.resources.stringResource

class ConfirmDeleteDialog : DialogOverlay<Boolean>() {
    @Composable
    override fun AnimatedVisibilityScope.AnimatedContent(
        navigator: OverlayNavigator<Boolean>,
        transitionController: OverlayTransitionController,
    ) {
        AppConfirmDialog(
            confirmText = stringResource(InstanceRes.string.deleteInstanceConfirmation),
            confirmButton = {
                AppIconTextButton(
                    onClick = { navigator.finish(false) },
                    text = stringResource(SharedRes.string.cancel),
                    imageVector = Lucide.X,
                )
            },
            dismissButton = {
                AppIconTextButton(
                    onClick = { navigator.finish(true) },
                    text = stringResource(SharedRes.string.delete),
                    imageVector = Lucide.Trash,
                    backgroundColor = Theme[colors][error],
                    contentColor = Theme[colors][onError]
                )
            },
            onClickOutside = { navigator.finish(false) },
        )
    }
}
