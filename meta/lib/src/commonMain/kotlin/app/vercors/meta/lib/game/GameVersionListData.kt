package app.vercors.meta.lib.game

import kotlinx.serialization.Serializable

@Serializable
data class GameVersionListData(
    val latestRelease: String,
    val latestSnapshot: String,
    val versions: List<BriefGameVersionData>
) {
}
