package app.vercors.feature.instance.data

import app.vercors.feature.game.domain.model.ModLoaderType

internal const val PRISM_MINECRAFT_UID = "net.minecraft"

internal const val PRISM_INTERMEDIARY_UID = "net.fabricmc.intermediary"

internal fun String.toPrismModLoaderTypeOrNull(): ModLoaderType? = when (this) {
    "net.minecraftforge" -> ModLoaderType.Forge
    "net.neoforged" -> ModLoaderType.NeoForge
    "net.fabricmc.fabric-loader" -> ModLoaderType.Fabric
    "org.quiltmc.quilt-loader" -> ModLoaderType.Quilt
    else -> null
}

internal fun ModLoaderType.toPrismUid(): String = when (this) {
    ModLoaderType.Forge -> "net.minecraftforge"
    ModLoaderType.NeoForge -> "net.neoforged"
    ModLoaderType.Fabric -> "net.fabricmc.fabric-loader"
    ModLoaderType.Quilt -> "org.quiltmc.quilt-loader"
}

internal fun String.toManagedPackPlatformIdOrNull(): String? = when (this) {
    "flame" -> "curseforge"
    "modrinth" -> "modrinth"
    else -> null
}

internal fun String.toManagedPackTypeOrNull(): String? = when (this) {
    "curseforge" -> "flame"
    "modrinth" -> "modrinth"
    else -> null
}
