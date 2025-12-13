package app.vercors.shared.domain.game.model

data class GameVersionList(
    val latestRelease: String,
    val latestSnapshot: String,
    val versions: List<BriefGameVersion>
)
