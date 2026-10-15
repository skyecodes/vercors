package app.vercors.feature.config.ui.about

import androidx.compose.runtime.Immutable
import app.vercors.feature.config.domain.model.LauncherUpdateState
import app.vercors.feature.config.domain.model.UpdateChannel

@Immutable
data class AboutConfigurationState(
    val appName: String,
    val appVersion: String,
    val checkForUpdatesOnStart: Boolean?,
    val updateChannel: UpdateChannel?,
    val launcherUpdate: LauncherUpdateState,
    val promptRestartForUpdate: Boolean,
)
