package app.vercors.feature.project.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProjectAuthorData(
    val name: String,
    val url: String? = null,
    val avatarUrl: String? = null
)
