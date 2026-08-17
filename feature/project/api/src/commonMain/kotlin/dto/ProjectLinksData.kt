package app.vercors.feature.project.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProjectLinksData(
    val websiteUrl: String? = null,
    val sourceUrl: String? = null,
    val issuesUrl: String? = null,
    val wikiUrl: String? = null,
    val discordUrl: String? = null
)
