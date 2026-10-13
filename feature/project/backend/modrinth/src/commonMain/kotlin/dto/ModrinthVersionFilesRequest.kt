package app.vercors.feature.project.backend.modrinth.dto

import kotlinx.serialization.Serializable

@Serializable
data class ModrinthVersionFilesRequest(
    val hashes: List<String>,
    val algorithm: String
)
