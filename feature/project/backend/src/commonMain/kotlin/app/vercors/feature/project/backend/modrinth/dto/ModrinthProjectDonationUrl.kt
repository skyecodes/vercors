package app.vercors.feature.project.backend.modrinth.dto


import kotlinx.serialization.Serializable

@Serializable
data class ModrinthProjectDonationUrl(
    val id: String,
    val platform: String,
    val url: String
)
