package app.vercors.feature.instance.ui.details.update

import androidx.compose.runtime.Immutable
import app.vercors.feature.instance.ui.details.LocalPackUpdatePrompt
import app.vercors.feature.instance.ui.details.ModpackUpdateView

@Immutable
data class InstanceUpdateState(
    val modpackUpdate: ModpackUpdateView?,
    val promptModpackUpdate: Boolean,
    val localPackUpdatePrompt: LocalPackUpdatePrompt?,
)
