package app.vercors.feature.project.backend.modrinth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModrinthVersionDependency(
    @SerialName("project_id")
    val projectId: String? = null,
    @SerialName("dependency_type")
    val dependencyType: ModrinthDependencyType
)
