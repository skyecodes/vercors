package app.vercors.feature.config.domain.model

import app.vercors.core.domain.DownloadProgress

sealed interface LauncherUpdateState {
    data object Idle : LauncherUpdateState
    data object Checking : LauncherUpdateState
    data object UpToDate : LauncherUpdateState

    sealed interface Found : LauncherUpdateState {
        val version: String
        val releaseUrl: String
    }

    data class Available(
        override val version: String,
        override val releaseUrl: String,
        val canInstall: Boolean,
    ) : Found

    data class Downloading(
        override val version: String,
        override val releaseUrl: String,
        val download: DownloadProgress?,
    ) : Found

    data class ReadyToInstall(override val version: String, override val releaseUrl: String) : Found
    data class Restarting(override val version: String, override val releaseUrl: String) : Found
    data class Failed(val error: LauncherUpdateError) : LauncherUpdateState
}

val LauncherUpdateState.isUpdateAvailable: Boolean
    get() = this is LauncherUpdateState.Found
