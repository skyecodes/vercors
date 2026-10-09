package app.vercors.feature.instance.domain.model

sealed interface InstanceFolderError {
    data object OpenFailed : InstanceFolderError
}
