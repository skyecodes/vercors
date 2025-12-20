package app.vercors.meta.backend.project.modrinth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModrinthCategory(
    val icon: String,
    val name: String,
    @SerialName("project_type")
    val projectType: ModrinthProjectType,
    val header: String
)
