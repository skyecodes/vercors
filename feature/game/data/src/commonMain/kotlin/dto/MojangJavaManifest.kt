package app.vercors.feature.game.data.dto

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** Keyed by OS/arch id (e.g. "linux", "windows-x64", "mac-os-arm64"), then by runtime component name
 *  (e.g. "java-runtime-gamma"), matching Mojang's `all.json` schema. */
typealias MojangJavaManifest = Map<String, Map<String, List<MojangJavaManifestEntry>>>

@Serializable
data class MojangJavaManifestEntry(
    val availability: Availability,
    val manifest: Manifest,
    val version: Version
) {
    @Serializable
    data class Availability(
        val group: Int,
        val progress: Int
    )

    @Serializable
    data class Manifest(
        val sha1: String,
        val size: Long,
        val url: String
    )

    @Serializable
    data class Version(
        val name: String,
        val released: Instant
    )
}
