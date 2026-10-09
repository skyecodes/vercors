package app.vercors.feature.instance.domain.model

sealed interface ImportError {
    data object UnsupportedFileType : ImportError
    data object CopyFailed : ImportError
}
