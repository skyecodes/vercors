package app.vercors.feature.project.backend.modrinth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val datePublished: ModrinthInstant,
    val files: List<ModrinthVersionFile> = emptyList(),
    val dependencies: List<ModrinthVersionDependency> = emptyList()
)

@Serializable
data class ModrinthVersionDependency(
    @SerialName("project_id")
    val projectId: String? = null,
    @SerialName("dependency_type")
    val dependencyType: ModrinthDependencyType
)

@JvmInline
@Serializable
value class ModrinthDependencyType(val value: String) {
    companion object {
        const val REQUIRED = "required"
    }
}

@Serializable
data class ModrinthVersionFile(
    val hashes: Map<String, String> = emptyMap(),
    val url: String,
    val filename: String,
    val primary: Boolean = false,
    val size: Long
)

@JvmInline
@Serializable
value class ModrinthVersionType(val value: String) {
    companion object {
        const val RELEASE = "release"
        const val BETA = "beta"
        const val ALPHA = "alpha"
    }
}
