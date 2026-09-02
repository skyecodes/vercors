package app.vercors.feature.game.data.dto

import kotlinx.serialization.Serializable

/**
 * A *legacy* (pre-1.13) Forge installer's `install_profile.json` — used before Forge's processor/binary-patching
 * installer format (see [InstallProfileData]) was introduced. There's no separate `version.json`: the version
 * data is embedded directly as [versionInfo], and there's no client-jar patching step at all — [install] instead
 * names a ready-to-use "universal" jar (Forge's own mod-loading code for this era), bundled inside the installer
 * jar itself as a raw zip entry at [Install.filePath].
 */
@Serializable
data class LegacyForgeInstallProfile(
    val install: Install,
    val versionInfo: VersionInfo
) {
    @Serializable
    data class Install(
        /** The universal jar's entry name inside the installer jar, e.g. "forge-1.7.10-10.13.4.1614-1.7.10-universal.jar". */
        val filePath: String,
        /** The universal jar's maven coordinate (no classifier), e.g. "net.minecraftforge:forge:1.7.10-10.13.4.1614-1.7.10". */
        val path: String
    )

    @Serializable
    data class VersionInfo(
        val mainClass: String,
        /** Old-style single-string CLI args, e.g. "--username ${auth_player_name} ... --tweakClass ...". */
        val minecraftArguments: String? = null,
        val libraries: List<Library> = emptyList()
    )

    @Serializable
    data class Library(val name: String, val url: String? = null)
}
