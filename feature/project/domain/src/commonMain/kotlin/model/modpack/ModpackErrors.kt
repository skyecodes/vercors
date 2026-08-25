package app.vercors.feature.project.domain.model.modpack

sealed interface ModpackResolutionError

data object ModpackDownloadFailed : ModpackResolutionError
data object ModpackParseFailed : ModpackResolutionError
data object ModpackUnsupportedPlatform : ModpackResolutionError
data object ModpackFileResolutionFailed : ModpackResolutionError

sealed interface ModpackInstallError

data object ModpackInstallDownloadFailed : ModpackInstallError
data object ModpackOverridesExtractionFailed : ModpackInstallError
data object ModpackInvalidFileEntry : ModpackInstallError
