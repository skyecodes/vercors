package app.vercors.feature.project.domain.model

sealed interface ManualDownloadError {
    data object DownloadFailed : ManualDownloadError
    data object IntegrityMismatch : ManualDownloadError
}
