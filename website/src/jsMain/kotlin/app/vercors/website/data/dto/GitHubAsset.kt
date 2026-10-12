package app.vercors.website.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubAsset(
    val name: String,
    val size: Long,
    @SerialName("browser_download_url") val browserDownloadUrl: String,
)
