package app.vercors.feature.game.data.dto

import kotlinx.serialization.Serializable

/**
 * Fabric's and Quilt's `.../profile/json` response. Shares [MojangVersionInfo.Arguments]' shape for
 * `mainClass`/`arguments`, but — unlike Forge/NeoForge's embedded `version.json` — its `libraries` are a flat,
 * non-Mojang schema: `{name, url, sha1?, size?}` rather than `{name, downloads: {artifact: {path, url, sha1, size}}}`.
 * `url` is the maven repo base the artifact lives under; `sha1`/`size` are absent for some entries (e.g. the
 * loader/intermediary jars themselves).
 */
@Serializable
data class FabricLoaderProfile(
    val mainClass: String,
    val arguments: MojangVersionInfo.Arguments? = null,
    val libraries: List<Library> = emptyList()
) {
    @Serializable
    data class Library(
        val name: String,
        val url: String,
        val sha1: String? = null,
        val size: Long? = null
    )
}
