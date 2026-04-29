package app.vercors.feature.project.backend.modrinth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModrinthUser(
    val id: String,
    val username: String,
    @SerialName("avatar_url")
    val avatarUrl: String,
    val created: ModrinthInstant,
    val role: ModrinthRole,
    val name: String? = null,
    val bio: String? = null,
    val badges: Int? = null,
)
