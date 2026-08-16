package app.vercors.feature.game.data.dto

import kotlinx.serialization.Serializable

/**
 * The Mojang-shaped version-json fragment a mod loader contributes: Fabric's and Quilt's `.../profile/json`
 * endpoints return this directly, and it's also the shape of the `version.json` produced inside a Forge/NeoForge
 * installer jar once its processors have run. Reuses [MojangVersionInfo]'s nested types since the schema is
 * identical, just without the vanilla-only fields (`downloads`, `assetIndex`, `assets`, ...).
 */
@Serializable
data class LoaderVersionProfile(
    val mainClass: String,
    val arguments: MojangVersionInfo.Arguments? = null,
    val libraries: List<MojangVersionInfo.Library> = emptyList(),
    // Artifacts an FML-based installer produces locally (e.g. the patched client jar) that its own runtime
    // locator finds by scanning the library directory — never put on the classpath, but still worth caching
    // here so a later launch can confirm they still exist before trusting the rest of this cached profile.
    val localArtifacts: List<MojangVersionInfo.Library.Downloads.Artifact> = emptyList()
)
