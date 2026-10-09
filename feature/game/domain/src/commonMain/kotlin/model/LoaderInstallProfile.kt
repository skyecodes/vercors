package app.vercors.feature.game.domain.model

data class LoaderInstallProfile(
    val mainClass: String,
    val libraries: List<GameVersionDetails.Library>,
    val gameArgs: List<GameVersionDetails.Argument>?,
    val jvmArgs: List<GameVersionDetails.Argument>?,
    val omitClientFromClasspath: Boolean = false,
    // True only for legacy Forge: its minecraftArguments is already complete, so appending would duplicate every flag
    val replacesGameArgs: Boolean = false
)
