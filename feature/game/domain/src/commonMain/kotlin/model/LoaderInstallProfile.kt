package app.vercors.feature.game.domain.model

/** The extra pieces a mod loader contributes on top of the vanilla [GameVersionDetails] it installs onto. */
data class LoaderInstallProfile(
    val mainClass: String,
    val libraries: List<GameVersionDetails.Library>,
    val gameArgs: List<GameVersionDetails.Argument>?,
    val jvmArgs: List<GameVersionDetails.Argument>?,
    val omitClientFromClasspath: Boolean = false,
    // Modern loaders' arguments.game (Forge/NeoForge/Fabric/Quilt version.json) is a small, purely additive
    // list, meant to be appended after the vanilla version's own — same "inheritsFrom"-style merging the
    // official Mojang launcher uses. A legacy (pre-1.13) Forge profile's gameArgs come from tokenizing its
    // old single-string minecraftArguments, which is already a complete, self-sufficient argument list (it
    // textually repeats every standard flag itself, plus its own --tweakClass) — appending it after the
    // vanilla version's own (also minecraftArguments-derived, see MojangVersionInfo.toDomain's fallback)
    // gameArgs would duplicate every flag. Set true only for that case.
    val replacesGameArgs: Boolean = false
)
