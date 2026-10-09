package app.vercors.feature.instance.domain.model.localimport

sealed interface LocalImportError {
    data object NotPrismFormat : LocalImportError
    data object ParseFailed : LocalImportError
    data object ExtractionFailed : LocalImportError
}
