package app.vercors.meta.backend.project.modrinth.dto

import kotlinx.serialization.Serializable

@Serializable
data class ModrinthModeratorMessage(
    val message: String,
    val body: String? = null
)
