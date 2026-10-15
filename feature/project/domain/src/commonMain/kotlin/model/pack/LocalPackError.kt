package app.vercors.feature.project.domain.model.pack

sealed interface LocalPackError {
    data object UnsupportedFormat : LocalPackError
    data object ParseFailed : LocalPackError
    data object UnsupportedPackVersion : LocalPackError
    data object InstallFailed : LocalPackError
    data object InstanceNotFound : LocalPackError
}
