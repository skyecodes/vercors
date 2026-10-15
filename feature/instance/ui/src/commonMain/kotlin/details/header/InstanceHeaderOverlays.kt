package app.vercors.feature.instance.ui.details.header

import androidx.compose.runtime.Composable
import app.vercors.feature.instance.ui.deleteInstanceConfirmDialog
import app.vercors.shared.ui.Store
import com.slack.circuit.overlay.OverlayEffect

@Composable
fun InstanceHeaderOverlays(store: Store<InstanceHeaderState, InstanceHeaderEvent>) {
    val (state, eventSink) = store
    if (state.promptDeleteConfirmation) {
        OverlayEffect {
            eventSink(InstanceHeaderEvent.OnConfirmDelete(show(deleteInstanceConfirmDialog())))
        }
    }

    state.copyInstanceDialog?.let { dialog ->
        OverlayEffect(dialog) {
            eventSink(InstanceHeaderEvent.OnCopyInstanceResult(show(dialog)))
        }
    }

    state.exportInstanceDialog?.let { dialog ->
        OverlayEffect(dialog) {
            eventSink(InstanceHeaderEvent.OnExportInstanceResult(show(dialog)))
        }
    }

    state.copyOptionsDialog?.let { dialog ->
        OverlayEffect(dialog) {
            eventSink(InstanceHeaderEvent.OnCopyOptionsResult(show(dialog)))
        }
    }

    state.saveDefaultOptionsDialog?.let { dialog ->
        OverlayEffect(dialog) {
            eventSink(InstanceHeaderEvent.OnSaveDefaultOptionsResult(show(dialog)))
        }
    }
}
