package app.vercors.feature.instance.ui.actions

import androidx.compose.runtime.Composable
import app.vercors.feature.instance.ui.deleteInstanceConfirmDialog
import app.vercors.shared.ui.Store
import com.slack.circuit.overlay.OverlayEffect

@Composable
fun InstanceActionsOverlays(store: Store<InstanceActionsState, InstanceActionsEvent>) {
    val (state, eventSink) = store
    if (state.promptCreateInstance) {
        OverlayEffect {
            eventSink(InstanceActionsEvent.OnCreateInstanceResult(show(state.createInstanceDialog)))
        }
    }

    state.pendingDeleteInstanceId?.let {
        OverlayEffect(it) {
            eventSink(InstanceActionsEvent.OnConfirmDeleteInstance(show(deleteInstanceConfirmDialog())))
        }
    }

    state.copyInstanceDialog?.let { dialog ->
        OverlayEffect(dialog) {
            eventSink(InstanceActionsEvent.OnCopyInstanceResult(show(dialog)))
        }
    }

    state.exportInstanceDialog?.let { dialog ->
        OverlayEffect(dialog) {
            eventSink(InstanceActionsEvent.OnExportInstanceResult(show(dialog)))
        }
    }
}
