package app.vercors.feature.instance.domain.model

sealed interface InstanceStatus {
    data object Idle : InstanceStatus

    sealed interface Launching : InstanceStatus {
        data object ValidatingToken : Launching
        data object RefreshingToken : Launching
        data object LoadingMetadata : Launching
        data object ValidatingFiles : Launching
        data object DownloadingFiles : Launching
    }

    sealed interface Failed : InstanceStatus {
        data object LoginRequired : Failed
        data class TokenValidation(val error: Exception) : Failed
        data class TokenRefresh(val error: Exception) : Failed
        data class Unknown(val error: Exception) : Failed
    }

    data class Running(val stop: suspend () -> Unit) : InstanceStatus

    data class Stopping(val kill: suspend () -> Unit) : InstanceStatus
}
