package app.vercors.feature.instance.ui.details.installation

import androidx.compose.runtime.Composable
import app.vercors.shared.ui.Store
import com.slack.circuit.overlay.OverlayEffect

@Composable
fun InstanceInstallationOverlays(store: Store<InstanceInstallationState, InstanceInstallationEvent>) {
    val (state, eventSink) = store
    if (state.promptFinishInstallation) {
        OverlayEffect {
            show(state.finishInstallationDialog)
            eventSink(InstanceInstallationEvent.OnFinishInstallationDismiss)
        }
    }
}
