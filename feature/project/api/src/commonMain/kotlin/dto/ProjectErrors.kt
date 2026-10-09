package app.vercors.feature.project.api.dto

import kotlinx.serialization.Serializable

@Serializable
sealed interface PlatformOrProjectNotFoundData

@Serializable
data class ProjectNotFoundData(val platformId: String, val notFoundProjectId: String) : PlatformOrProjectNotFoundData

@Serializable
data class PlatformNotFoundData(val notFoundPlatformId: String) : PlatformOrProjectNotFoundData

@Serializable
data class PlatformErrorData(
    val error: String,
    val description: String? = null
)
