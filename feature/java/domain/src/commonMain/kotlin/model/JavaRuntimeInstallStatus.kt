package app.vercors.feature.java.domain.model

import app.vercors.core.domain.DownloadProgress

sealed interface JavaRuntimeInstallStatus {
    data class Downloading(val download: DownloadProgress?) : JavaRuntimeInstallStatus
    data object Done : JavaRuntimeInstallStatus
}
