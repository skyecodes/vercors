package app.vercors.feature.project.backend.modrinth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ModrinthVersion(
    val id: String,
    @SerialName("project_id")
    val projectId: String,
    val name: String,
    @SerialName("version_number")
    val versionNumber: String,
    @SerialName("game_versions")
    val gameVersions: List<String> = emptyList(),
    @SerialName("version_type")
    val versionType: ModrinthVersionType,
    val loaders: List<String> = emptyList(),
    @SerialName("date_published")
    val datePublished: Instant,
    val files: List<ModrinthVersionFile> = emptyList(),
    val dependencies: List<ModrinthVersionDependency> = emptyList()
)
