package app.vercors.feature.game.domain.model

data class GameVersionList(
    val latestRelease: String,
    val latestSnapshot: String,
    val versions: List<BriefGameVersion>
)

data class BriefGameVersion(
    val id: String,
    val type: GameVersionType,
)
