package app.vercors.meta.backend.game.mojang.dto


import kotlinx.serialization.Serializable

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
        val releaseTime: MojangInstant,
        val sha1: String,
        val time: MojangInstant,
        val type: MojangReleaseType,
        val url: String
    )
}