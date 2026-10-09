package app.vercors.feature.instance.domain.model

sealed interface InstanceIconError {
    data object DownloadFailed : InstanceIconError
}
