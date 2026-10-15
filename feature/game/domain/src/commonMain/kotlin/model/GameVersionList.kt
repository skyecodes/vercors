package app.vercors.feature.game.domain.model

import kotlin.time.Instant

data class GameVersionList(
    val latestRelease: String,
    val latestSnapshot: String,
    val versions: List<Version>
) {
    data class Version(
        val id: String,
        val type: GameVersionType,
        val url: String,
        val sha1: String,
        val releaseTime: Instant
    )
}
