package app.vercors.feature.project.data.modpack

import app.vercors.feature.game.domain.model.ModLoaderType

internal const val MRPACK_MINECRAFT_KEY = "minecraft"

internal fun String.toMrpackModLoaderTypeOrNull(): ModLoaderType? = when (this) {
    "forge" -> ModLoaderType.Forge
    "neoforge" -> ModLoaderType.NeoForge
    "fabric-loader" -> ModLoaderType.Fabric
    "quilt-loader" -> ModLoaderType.Quilt
    else -> null
}

internal fun ModLoaderType.toMrpackDependencyKey(): String = when (this) {
    ModLoaderType.Forge -> "forge"
    ModLoaderType.NeoForge -> "neoforge"
    ModLoaderType.Fabric -> "fabric-loader"
    ModLoaderType.Quilt -> "quilt-loader"
}
