package app.vercors.feature.instance.domain.model.localimport

sealed interface LocalImportError

data object LocalImportNotPrismFormat : LocalImportError
data object LocalImportParseFailed : LocalImportError
data object LocalImportExtractionFailed : LocalImportError
