package app.vercors.feature.config.domain.model

sealed interface LauncherUpdateState {
    data object Idle : LauncherUpdateState
    data object Checking : LauncherUpdateState
    data object UpToDate : LauncherUpdateState
    data class Available(val version: String, val releaseUrl: String, val canInstall: Boolean) : LauncherUpdateState
    data class Downloading(val version: String, val releaseUrl: String, val progress: Float?) : LauncherUpdateState
    data class ReadyToInstall(val version: String, val releaseUrl: String) : LauncherUpdateState
    data class Failed(val error: LauncherUpdateError) : LauncherUpdateState
}

val LauncherUpdateState.isUpdateAvailable: Boolean
    get() = this is LauncherUpdateState.Available ||
            this is LauncherUpdateState.Downloading ||
            this is LauncherUpdateState.ReadyToInstall
