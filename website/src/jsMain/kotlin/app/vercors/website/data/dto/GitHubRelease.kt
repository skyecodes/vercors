package app.vercors.website.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubRelease(
    @SerialName("tag_name") val tagName: String,
    @SerialName("html_url") val htmlUrl: String,
    @SerialName("published_at") val publishedAt: String? = null,
    val draft: Boolean,
    val prerelease: Boolean,
    val assets: List<GitHubAsset>,
)
