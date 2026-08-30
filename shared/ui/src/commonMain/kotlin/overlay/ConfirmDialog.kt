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

/**
 * A [ConfirmDialogButton]'s accent. [Theme]'s color tokens (e.g. [error]/[onError]) are themselves
 * `@Composable` reads (animated on theme/accent change), so they can't be resolved at [ConfirmDialog]
 * construction time (built outside composition, inside `OverlayEffect`'s suspend block) — [ConfirmDialogButton]
 * carries this role instead, resolved inside [ConfirmDialogButton.Render].
 */
enum class ConfirmDialogButtonStyle {
    Neutral,
    Destructive,
    Primary,
}

/** One button of a [ConfirmDialog]. */
data class ConfirmDialogButton(
    val textResource: StringResource,
    val icon: ImageVector,
    val style: ConfirmDialogButtonStyle = ConfirmDialogButtonStyle.Neutral,
)

/**
 * Generic yes/no confirmation dialog: a message plus a neutral "cancel" button (returns `false`) and an
 * accented "action" button (returns `true`). Replaces the former `ConfirmDeleteDialog`/`ConfirmRemoveDialog`/
 * `ConfirmInstallJavaDialog` (`feature/instance/ui`), which only differed in message/button text/icon/color.
 */
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
