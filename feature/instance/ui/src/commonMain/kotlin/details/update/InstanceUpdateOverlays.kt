package app.vercors.feature.instance.ui.details.update

import androidx.compose.runtime.Composable
import app.vercors.feature.instance.ui.*
import app.vercors.feature.instance.ui.details.LocalPackUpdateKind
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.overlay.ConfirmDialog
import app.vercors.shared.ui.overlay.ConfirmDialogButton
import app.vercors.shared.ui.overlay.ConfirmDialogButtonStyle
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.Lucide
import com.slack.circuit.overlay.OverlayEffect
import org.jetbrains.compose.resources.StringResource

@Composable
fun InstanceUpdateOverlays(store: Store<InstanceUpdateState, InstanceUpdateEvent>) {
    val (state, eventSink) = store
    state.modpackUpdate?.takeIf { state.promptModpackUpdate }?.let { update ->
        OverlayEffect(update) {
            val result = show(
                ConfirmDialog(
                    messageResource = if (update.newGameVersion != null) {
                        InstanceRes.string.promptModpackUpdateWithGameVersion
                    } else {
                        InstanceRes.string.promptModpackUpdate
                    },
                    messageArgs = listOfNotNull(update.currentVersion, update.latestVersion, update.newGameVersion),
                    actionButton = ConfirmDialogButton(
                        textResource = InstanceRes.string.updateModpack,
                        icon = Lucide.Download,
                        style = ConfirmDialogButtonStyle.Primary,
                    ),
                )
            )
            eventSink(InstanceUpdateEvent.OnConfirmModpackUpdate(result))
        }
    }

    state.localPackUpdatePrompt?.let { prompt ->
        OverlayEffect(prompt) {
            val result = show(
                ConfirmDialog(
                    messageResource = prompt.kind.messageResource,
                    messageArgs = listOf(prompt.currentLabel, prompt.newLabel, prompt.gameVersion),
                    actionButton = ConfirmDialogButton(
                        textResource = InstanceRes.string.updateModpack,
                        icon = Lucide.Download,
                        style = ConfirmDialogButtonStyle.Primary,
                    ),
                )
            )
            eventSink(InstanceUpdateEvent.OnConfirmLocalPackUpdate(result))
        }
    }
}

private val LocalPackUpdateKind.messageResource: StringResource
    get() = when (this) {
        LocalPackUpdateKind.SamePack -> InstanceRes.string.promptLocalPackUpdate
        LocalPackUpdateKind.NoRecord -> InstanceRes.string.promptLocalPackUpdateNoRecord
        LocalPackUpdateKind.OtherPack -> InstanceRes.string.promptLocalPackUpdateOtherPack
    }
