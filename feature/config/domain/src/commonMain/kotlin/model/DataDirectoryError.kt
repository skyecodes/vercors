package app.vercors.feature.config.domain.model

sealed interface DataDirectoryError {
    data object Unreachable : DataDirectoryError
    data object CopyFailed : DataDirectoryError
}
