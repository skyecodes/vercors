package app.vercors.feature.game.domain.model

sealed interface JavaRuntimeError {
    data class InvalidManifest(val cause: Any) : JavaRuntimeError
    data class ComponentNotAvailable(val majorVersion: Int) : JavaRuntimeError
    data class DownloadFailed(val cause: Any) : JavaRuntimeError
    data object ProbeFailed : JavaRuntimeError
}

sealed interface JavaRuntimeInstallStatus {
    data class Downloading(val fraction: Float?) : JavaRuntimeInstallStatus
    data object Done : JavaRuntimeInstallStatus
}
