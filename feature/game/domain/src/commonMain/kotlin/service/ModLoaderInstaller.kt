package app.vercors.feature.game.domain.service

import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.game.domain.model.LoaderInstallProfile
import app.vercors.feature.game.domain.model.ModLoaderInstallError
import app.vercors.feature.game.domain.model.ModLoaderType
import arrow.core.raise.Raise

interface ModLoaderInstaller {
    context(_: Raise<ModLoaderInstallError>)
    suspend fun resolve(gameVersion: GameVersionDetails, loaderVersion: String): LoaderInstallProfile
}

val ModLoaderType.key: String
    get() = when (this) {
        ModLoaderType.Fabric -> "fabric"
        ModLoaderType.Quilt -> "quilt"
        ModLoaderType.Forge -> "forge"
        ModLoaderType.NeoForge -> "neoforge"
    }
