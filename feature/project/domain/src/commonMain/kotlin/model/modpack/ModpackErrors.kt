package app.vercors.feature.project.domain.model.modpack

sealed interface ModpackResolutionError {
    data object DownloadFailed : ModpackResolutionError
    data object ParseFailed : ModpackResolutionError
    data object UnsupportedPlatform : ModpackResolutionError
    data object FileResolutionFailed : ModpackResolutionError
}

sealed interface ModpackInstallError {
    data object DownloadFailed : ModpackInstallError
    data object OverridesExtractionFailed : ModpackInstallError
    data object InvalidFileEntry : ModpackInstallError
}
