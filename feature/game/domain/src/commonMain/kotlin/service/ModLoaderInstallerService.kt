package app.vercors.feature.game.domain.service

import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.game.domain.model.ModLoaderInstallError
import app.vercors.feature.game.domain.model.ModLoaderType
import arrow.core.raise.Raise
import arrow.core.raise.context.ensureNotNull
import dev.zacsweers.metro.Inject

@Inject
class ModLoaderInstallerService(
    private val installers: Map<String, ModLoaderInstaller>
) {
    context(_: Raise<ModLoaderInstallError>)
    suspend fun resolve(
        gameVersion: GameVersionDetails,
        loaderType: ModLoaderType,
        loaderVersion: String
    ): GameVersionDetails {
        val installer = ensureNotNull(installers[loaderType.key]) { ModLoaderInstallError.Unsupported(loaderType) }
        val profile = installer.resolve(gameVersion, loaderVersion)
        return gameVersion.mergeWith(profile)
    }
}
