package app.vercors.meta.lib.project

import kotlinx.serialization.Serializable

@Serializable
data class ProjectData(
    val id: String,
    val name: String,
    val description: String,
    val author: String,
    val url: String,
    val logoUrl: String?,
    val imageUrl: String?
)