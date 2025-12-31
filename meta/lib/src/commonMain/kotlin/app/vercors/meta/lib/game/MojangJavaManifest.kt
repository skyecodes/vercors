package app.vercors.meta.lib.game

import kotlinx.serialization.Serializable
import kotlin.time.Instant

typealias MojangJavaManifest = Map<String, List<MojangJavaManifestEntry>>

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
