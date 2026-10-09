package app.vercors.feature.game.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LegacyForgeInstallProfile(
    val install: Install,
    val versionInfo: VersionInfo
) {
    @Serializable
    data class Install(
        val filePath: String,
        val path: String
    )

    @Serializable
    data class VersionInfo(
        val mainClass: String,
        val minecraftArguments: String? = null,
        val libraries: List<Library> = emptyList()
    )

    @Serializable
    data class Library(val name: String, val url: String? = null)
}
