package app.vercors.feature.project.domain.model.pack

sealed interface VercorsPackError {
    data object NotVercorsFormat : VercorsPackError
    data object ParseFailed : VercorsPackError
    data object UnsupportedFormatVersion : VercorsPackError
}
