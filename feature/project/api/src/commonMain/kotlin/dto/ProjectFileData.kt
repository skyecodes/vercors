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
    val sha1: String? = null,
    val md5: String? = null,
    val publishedAt: Instant,
    val dependencies: List<ProjectFileDependencyData>,
    val manualDownloadUrl: String? = null,
    val type: ProjectTypeData? = null
)
