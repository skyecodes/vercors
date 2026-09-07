package app.vercors.feature.instance.domain.model

import app.vercors.feature.game.domain.model.ModLoaderType

sealed interface InstanceStatus {
    data object Idle : InstanceStatus

    sealed interface Launching : InstanceStatus {
        data object ValidatingToken : Launching
        data object RefreshingToken : Launching
        data object LoadingMetadata : Launching
        data class InstallingLoader(val loaderType: ModLoaderType) : Launching
        data class ValidatingFiles(val progress: Float?) : Launching
        data class DownloadingFiles(val progress: Float?) : Launching
    }

    sealed interface Installing : InstanceStatus {
        data class ResolvingFiles(val progress: Float?) : Installing
        data class DownloadingFiles(val progress: Float?) : Installing
        data object AwaitingManualDownloads : Installing
        data class Interrupted(val stage: InstanceInstallStage) : Installing

        /** Copying a PrismLauncher-imported instance's `.minecraft` folder contents into place. Purely transient (never
         *  persisted via [InstanceInstallStage]) — a local import that's interrupted mid-copy has no resumable
         *  state, unlike an online modpack install. */
        data class ExtractingFiles(val progress: Float?) : Installing
    }

    sealed interface Failed : InstanceStatus {
        data object LoginRequired : Failed
        data class TokenValidation(val error: Exception) : Failed
        data class TokenRefresh(val error: Exception) : Failed
        data class Unknown(val error: Exception) : Failed
        data object InstallFailed : Failed
    }

    data class Running(val stop: suspend () -> Unit) : InstanceStatus

    data class Stopping(val kill: suspend () -> Unit) : InstanceStatus
}
