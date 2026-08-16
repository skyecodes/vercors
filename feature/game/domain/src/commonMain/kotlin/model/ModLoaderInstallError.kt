package app.vercors.feature.game.domain.model

sealed interface ModLoaderInstallError {
    data class Unsupported(val loader: ModLoaderType) : ModLoaderInstallError
    data class FetchFailed(val loader: ModLoaderType, val cause: Any) : ModLoaderInstallError
    data class ProcessFailed(val loader: ModLoaderType, val processor: String, val exitCode: Int, val stderr: String) :
        ModLoaderInstallError
}
