package app.vercors.feature.project.api.dto

import kotlinx.serialization.Serializable

@Serializable
sealed interface PlatformOrProjectNotFoundData

@Serializable
data class ProjectNotFoundData(val projectId: String, val platformId: String) : PlatformOrProjectNotFoundData

@Serializable
data class PlatformNotFoundData(val platformId: String) : PlatformOrProjectNotFoundData

@Serializable
data class UnsupportedProjectTypeData(val projectType: ProjectTypeData)

@Serializable
data class PlatformErrorData(
    val error: String,
    val description: String?
)
