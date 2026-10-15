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

const val FABRIC_LOADER_KEY = "fabric"
const val QUILT_LOADER_KEY = "quilt"
const val FORGE_LOADER_KEY = "forge"
const val NEOFORGE_LOADER_KEY = "neoforge"

val ModLoaderType.key: String
    get() = when (this) {
        ModLoaderType.Fabric -> FABRIC_LOADER_KEY
        ModLoaderType.Quilt -> QUILT_LOADER_KEY
        ModLoaderType.Forge -> FORGE_LOADER_KEY
        ModLoaderType.NeoForge -> NEOFORGE_LOADER_KEY
    }
