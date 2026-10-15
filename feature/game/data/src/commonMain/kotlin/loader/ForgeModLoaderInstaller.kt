package app.vercors.feature.game.data.loader

import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.game.domain.model.LoaderInstallProfile
import app.vercors.feature.game.domain.model.ModLoaderInstallError
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.domain.service.FORGE_LOADER_KEY
import app.vercors.feature.game.domain.service.ModLoaderInstaller
import arrow.core.raise.Raise
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.StringKey

@SingleIn(AppScope::class)
@ContributesIntoMap(AppScope::class)
@StringKey(FORGE_LOADER_KEY)
class ForgeModLoaderInstaller(
    private val forgeFamilyInstaller: ForgeFamilyInstaller
) : ModLoaderInstaller {
    context(_: Raise<ModLoaderInstallError>)
    override suspend fun resolve(gameVersion: GameVersionDetails, loaderVersion: String): LoaderInstallProfile =
        forgeFamilyInstaller.install(
            loaderType = ModLoaderType.Forge,
            gameVersion = gameVersion,
            loaderVersion = loaderVersion
        )
}
