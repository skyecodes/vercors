package app.vercors.feature.project.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProjectData(
    val id: String,
    val platform: String,
    val name: String,
    val description: String,
    val body: String?,
    val url: String,
    val logoUrl: String?
)
