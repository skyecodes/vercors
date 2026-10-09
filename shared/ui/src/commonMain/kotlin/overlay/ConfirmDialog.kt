package app.vercors.shared.ui.overlay

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import app.vercors.core.ui.component.AppIconTextButton
import app.vercors.core.ui.layout.AppConfirmDialog
import app.vercors.core.ui.theme.*
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.cancel
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.composeunstyled.theme.Theme
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.overlay.OverlayTransitionController
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

// Theme colors are @Composable reads, so the role is resolved at render time rather than at construction
enum class ConfirmDialogButtonStyle {
    Neutral,
    Destructive,
    Primary,
}

data class ConfirmDialogButton(
    val textResource: StringResource,
    val icon: ImageVector,
    val style: ConfirmDialogButtonStyle = ConfirmDialogButtonStyle.Neutral,
)

class ConfirmDialog(
    private val messageResource: StringResource,
    private val messageArgs: List<Any> = emptyList(),
    private val actionButton: ConfirmDialogButton,
    private val cancelButton: ConfirmDialogButton = ConfirmDialogButton(SharedRes.string.cancel, Lucide.X),
) : DialogOverlay<Boolean>() {
    @Composable
    override fun AnimatedVisibilityScope.AnimatedContent(
        navigator: OverlayNavigator<Boolean>,
        transitionController: OverlayTransitionController,
    ) {
        AppConfirmDialog(
            confirmText = stringResource(messageResource, *messageArgs.toTypedArray()),
            confirmButton = { cancelButton.Render(onClick = { navigator.finish(false) }) },
            dismissButton = { actionButton.Render(onClick = { navigator.finish(true) }) },
            onClickOutside = { navigator.finish(false) },
        )
    }
}

@Composable
private fun ConfirmDialogButton.Render(onClick: () -> Unit) {
    val text = stringResource(textResource)
    when (style) {
        ConfirmDialogButtonStyle.Neutral -> AppIconTextButton(onClick = onClick, text = text, imageVector = icon)
        ConfirmDialogButtonStyle.Destructive -> AppIconTextButton(
            onClick = onClick,
            text = text,
            imageVector = icon,
            backgroundColor = Theme[colors][error],
            contentColor = Theme[colors][onError],
        )

        ConfirmDialogButtonStyle.Primary -> AppIconTextButton(
            onClick = onClick,
            text = text,
            imageVector = icon,
            backgroundColor = Theme[colors][primary],
            contentColor = Theme[colors][onPrimary],
        )
    }
}
