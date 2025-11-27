package app.vercors.meta.lib.game

import kotlinx.serialization.Serializable

@Serializable
data class GameVersionListData(
    val latestRelease: String,
    val latestSnapshot: String,
    val versions: List<Version>
) {
    @Serializable
    data class Version(
        val id: String,
        val type: GameVersionTypeData,
    )
}
