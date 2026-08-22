package app.vercors.feature.instance.ui.details

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import app.vercors.core.ui.component.AppIconTextButton
import app.vercors.core.ui.layout.AppConfirmDialog
import app.vercors.feature.instance.ui.InstanceRes
import app.vercors.feature.instance.ui.installJava
import app.vercors.feature.instance.ui.promptInstallJavaMessage
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.cancel
import app.vercors.shared.ui.overlay.DialogOverlay
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.overlay.OverlayTransitionController
import org.jetbrains.compose.resources.stringResource

class ConfirmInstallJavaDialog(private val majorVersion: Int) : DialogOverlay<Boolean>() {
    @Composable
    override fun AnimatedVisibilityScope.AnimatedContent(
        navigator: OverlayNavigator<Boolean>,
        transitionController: OverlayTransitionController,
    ) {
        AppConfirmDialog(
            confirmText = stringResource(InstanceRes.string.promptInstallJavaMessage, majorVersion),
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
                    text = stringResource(InstanceRes.string.installJava),
                    imageVector = Lucide.Download,
                )
            },
            onClickOutside = { navigator.finish(false) },
        )
    }
}
