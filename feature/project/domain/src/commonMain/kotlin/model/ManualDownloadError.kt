package app.vercors.feature.project.domain.model

sealed interface ManualDownloadError

/** Raised when the OS "Downloads" folder can't be resolved, or the detected file can't be moved into place. */
data object ManualDownloadFailed : ManualDownloadError
