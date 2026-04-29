package app.vercors.feature.game.domain.model

data class GameAssetIndex(
    val objects: Map<String, AssetObject>
) {
    data class AssetObject(
        val hash: String,
        val size: Long
    )
}
