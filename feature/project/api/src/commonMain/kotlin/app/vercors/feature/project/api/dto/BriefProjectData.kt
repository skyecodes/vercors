package app.vercors.feature.project.api.dto

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class BriefProjectData(
    val id: String,
    val platform: String,
    val name: String,
    val description: String,
    val author: String,
    val url: String,
    val downloads: Long,
    val lastUpdated: Instant,
    val logoUrl: String? = null,
    val imageUrl: String? = null
)
