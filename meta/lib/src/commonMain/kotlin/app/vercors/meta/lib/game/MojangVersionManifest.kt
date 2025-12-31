package app.vercors.meta.lib.game

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class MojangVersionManifest(
    val latest: Latest,
    val versions: List<Version>
) {
    @Serializable
    data class Latest(
        val release: String,
        val snapshot: String
    )

    @Serializable
    data class Version(
        val complianceLevel: Int,
        val id: String,
        val releaseTime: Instant,
        val sha1: String,
        val time: Instant,
        val type: MojangReleaseType,
        val url: String
    )
}
