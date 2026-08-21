package app.vercors.feature.instance.domain.model

sealed interface InstanceStatus {
    data object Idle : InstanceStatus

    sealed interface Launching : InstanceStatus {
        data object ValidatingToken : Launching
        data object RefreshingToken : Launching
        data object LoadingMetadata : Launching
        data class ValidatingFiles(val progress: Float?) : Launching
        data class DownloadingFiles(val progress: Float?) : Launching
    }

    sealed interface Installing : InstanceStatus {
        data class ResolvingFiles(val progress: Float?) : Installing
        data class DownloadingFiles(val progress: Float?) : Installing
        data object AwaitingManualDownloads : Installing
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
