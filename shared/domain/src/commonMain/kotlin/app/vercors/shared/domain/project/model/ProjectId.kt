package app.vercors.shared.domain.project.model

import kotlinx.serialization.Serializable

@Serializable
data class ProjectId(
    val id: String,
    val platform: PlatformId
)
