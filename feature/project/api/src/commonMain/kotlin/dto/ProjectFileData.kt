package app.vercors.feature.project.api.dto

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ProjectFileData(
    val id: String,
    val displayName: String,
    val fileName: String,
    val gameVersions: List<String>,
    val loaders: List<ProjectLoaderData>,
    val releaseChannel: ProjectReleaseChannelData,
    val size: Long,
    val sha1: String?,
    val publishedAt: Instant,
    val dependencies: List<ProjectFileDependencyData>
)
