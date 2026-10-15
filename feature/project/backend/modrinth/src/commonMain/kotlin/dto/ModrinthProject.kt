package app.vercors.feature.project.backend.modrinth.dto


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ModrinthProject(
    @SerialName("additional_categories")
    val additionalCategories: List<String> = emptyList(),
    val approved: Instant? = null,
    val body: String? = null,
    @SerialName("body_url")
    val bodyUrl: String? = null,
    val categories: List<String>,
    @SerialName("client_side")
    val clientSide: ModrinthProjectSideSupport = ModrinthProjectSideSupport.Unknown,
    val color: Int? = null,
    val description: String,
    @SerialName("discord_url")
    val discordUrl: String? = null,
    @SerialName("donation_urls")
    val donationUrls: List<ModrinthProjectDonationUrl> = emptyList(),
    val downloads: Int,
    val followers: Int,
    val gallery: List<ModrinthProjectGallery> = emptyList(),
    @SerialName("game_versions")
    val gameVersions: List<String> = emptyList(),
    @SerialName("icon_url")
    val iconUrl: String? = null,
    val id: String,
    @SerialName("issues_url")
    val issuesUrl: String? = null,
    val license: ModrinthLicense? = null,
    val loaders: List<String> = emptyList(),
    val moderatorMessage: ModrinthModeratorMessage? = null,
    @SerialName("monetization_status")
    val monetizationStatus: ModrinthProjectMonetizationStatus? = null,
    @SerialName("project_type")
    val projectType: ModrinthProjectType,
    val published: Instant,
    val queued: Instant? = null,
    @SerialName("requested_status")
    val requestedStatus: ModrinthProjectStatus? = null,
    @SerialName("server_side")
    val serverSide: ModrinthProjectSideSupport = ModrinthProjectSideSupport.Unknown,
    val slug: String,
    @SerialName("source_url")
    val sourceUrl: String? = null,
    val status: ModrinthProjectStatus? = null,
    val team: String,
    @SerialName("thread_id")
    val threadId: String? = null,
    val title: String,
    val updated: Instant,
    val versions: List<String> = emptyList(),
    @SerialName("wiki_url")
    val wikiUrl: String? = null
)
