package app.vercors.meta.backend.game.mojang.dto

import kotlinx.serialization.Serializable

@Serializable
data class MojangAssetIndex(
    val objects: Map<String, Asset>
) {
    @Serializable
    data class Asset(
        val hash: String,
        val size: Long
    )
}
