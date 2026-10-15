package app.vercors.feature.instance.ui.details.launch

import androidx.compose.runtime.Composable
import app.vercors.feature.instance.ui.InstanceRes
import app.vercors.feature.instance.ui.installJava
import app.vercors.feature.instance.ui.promptInstallJavaMessage
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.overlay.ConfirmDialog
import app.vercors.shared.ui.overlay.ConfirmDialogButton
import app.vercors.shared.ui.overlay.ConfirmDialogButtonStyle
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.Lucide
import com.slack.circuit.overlay.OverlayEffect

@Composable
fun InstanceLaunchOverlays(store: Store<InstanceLaunchState, InstanceLaunchEvent>) {
    val (state, eventSink) = store
    state.promptInstallJava?.let { majorVersion ->
        OverlayEffect(majorVersion) {
            val result = show(
                ConfirmDialog(
                    messageResource = InstanceRes.string.promptInstallJavaMessage,
                    messageArgs = listOf(majorVersion),
                    actionButton = ConfirmDialogButton(
                        textResource = InstanceRes.string.installJava,
                        icon = Lucide.Download,
                        style = ConfirmDialogButtonStyle.Primary,
                    ),
                )
            )
            if (result) eventSink(InstanceLaunchEvent.OnInstallJavaConfirm)
            else eventSink(InstanceLaunchEvent.OnInstallJavaDismiss)
        }
    }

    if (state.promptJavaInstallProgress) {
        OverlayEffect {
            show(state.installJavaProgressDialog)
            eventSink(InstanceLaunchEvent.OnJavaInstallProgressDismiss)
        }
    }

    if (state.promptLogin) {
        OverlayEffect {
            show(state.loginDialog)
            eventSink(InstanceLaunchEvent.OnLoginDismiss)
        }
    }
}
