package app.vercors.feature.java.domain.model

sealed interface JavaRuntimeInstallStatus {
    data class Downloading(val fraction: Float?) : JavaRuntimeInstallStatus
    data object Done : JavaRuntimeInstallStatus
}
