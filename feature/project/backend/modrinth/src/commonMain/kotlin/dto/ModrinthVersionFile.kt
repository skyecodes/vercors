package app.vercors.feature.project.backend.modrinth.dto

import kotlinx.serialization.Serializable

@Serializable
data class ModrinthVersionFile(
    val hashes: Map<String, String> = emptyMap(),
    val url: String,
    val filename: String,
    val primary: Boolean = false,
    val size: Long
)
