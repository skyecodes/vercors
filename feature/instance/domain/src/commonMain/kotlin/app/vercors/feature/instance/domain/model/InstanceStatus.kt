package app.vercors.feature.instance.domain.model

sealed interface InstanceStatus {
    data object Idle : InstanceStatus

    sealed interface Launching : InstanceStatus {
        val progress: Float?

        data object ValidatingToken : Launching {
            override val progress: Float? = null
        }

        data class RefreshingToken(override val progress: Float? = null) : Launching

        data object LoadingMetadata : Launching {
            override val progress: Float? = null
        }

        data class ValidatingFiles(override val progress: Float?) : Launching
        data class DownloadingFiles(override val progress: Float?) : Launching
    }

    sealed interface Failed : InstanceStatus {
        data object LoginRequired : Failed
        data class TokenValidation(val error: Exception) : Failed
        data class TokenRefresh(val error: Exception) : Failed
        data class Unknown(val error: Exception) : Failed
    }

    data object Running : InstanceStatus
}
