package app.vercors.feature.instance.ui.details.installation

import androidx.compose.runtime.Immutable
import app.vercors.feature.instance.ui.install.FinishInstallationDialog

@Immutable
data class InstanceInstallationState(
    val promptFinishInstallation: Boolean,
    val finishInstallationDialog: FinishInstallationDialog,
)
