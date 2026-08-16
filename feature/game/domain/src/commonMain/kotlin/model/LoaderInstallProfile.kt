package app.vercors.feature.game.domain.model

/** The extra pieces a mod loader contributes on top of the vanilla [GameVersionDetails] it installs onto. */
data class LoaderInstallProfile(
    val mainClass: String,
    val libraries: List<GameVersionDetails.Library>,
    val gameArgs: List<GameVersionDetails.Argument>?,
    val jvmArgs: List<GameVersionDetails.Argument>?,
    val omitClientFromClasspath: Boolean = false
)
