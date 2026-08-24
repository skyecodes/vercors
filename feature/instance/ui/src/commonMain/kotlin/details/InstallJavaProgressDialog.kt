package app.vercors.feature.instance.ui.details

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.animation.appAnimateFloatAsState
import app.vercors.core.ui.component.AppIconTextButton
import app.vercors.core.ui.component.AppProgressIndicator
import app.vercors.core.ui.component.AppText
import app.vercors.core.ui.layout.AppDialog
import app.vercors.core.ui.layout.AppError
import app.vercors.core.ui.layout.AppForm
import app.vercors.core.ui.theme.*
import app.vercors.feature.instance.ui.InstanceRes
import app.vercors.feature.instance.ui.javaInstallStarted
import app.vercors.feature.instance.ui.javaInstallSucceeded
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.cancel
import app.vercors.shared.ui.done
import app.vercors.shared.ui.overlay.DialogOverlay
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.composeunstyled.theme.Theme
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.overlay.OverlayTransitionController
import org.jetbrains.compose.resources.stringResource

class InstallJavaProgressDialog(
    private val state: State<JavaInstallProgressView>,
) : DialogOverlay<Unit>() {
    @Composable
    override fun AnimatedVisibilityScope.AnimatedContent(
        navigator: OverlayNavigator<Unit>,
        transitionController: OverlayTransitionController,
    ) {
        val view by state

        AppDialog(onClickOutside = {}) {
            AppForm {
                AppText(
                    text = stringResource(InstanceRes.string.javaInstallStarted, view.majorVersion),
                    style = Theme[textStyles][title]
                )

                if (!view.done) {
                    view.progress?.let {
                        val progress by appAnimateFloatAsState(it)
                        AppProgressIndicator(progress = progress, modifier = Modifier.width(500.dp))
                    }
                    AppIconTextButton(
                        onClick = { navigator.finish(Unit) },
                        text = stringResource(SharedRes.string.cancel),
                        imageVector = Lucide.X,
                        backgroundColor = Theme[colors][error],
                        contentColor = Theme[colors][onError],
                    )
                } else {
                    view.error?.let { errorMessage -> AppError(errorMessage()) }
                        ?: AppText(stringResource(InstanceRes.string.javaInstallSucceeded, view.majorVersion))
                    AppIconTextButton(
                        onClick = { navigator.finish(Unit) },
                        text = stringResource(SharedRes.string.done),
                        imageVector = Lucide.Check,
                    )
                }
            }
        }
    }
}
