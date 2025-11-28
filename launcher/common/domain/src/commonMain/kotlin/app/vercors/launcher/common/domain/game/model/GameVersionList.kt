package app.vercors.launcher.common.domain.game.model

data class GameVersionList(
    val latestRelease: String,
    val latestSnapshot: String,
    val versions: List<Version>
) {
    data class Version(
        val id: String,
        val type: GameVersionType,
    )
}
