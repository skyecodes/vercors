package app.vercors.feature.project.domain.model

import app.vercors.feature.game.domain.model.ModLoaderType
import kotlin.time.Instant

data class ProjectFile(
    val id: String,
    val displayName: String,
    val fileName: String,
    val gameVersions: List<String>,
    val loaders: List<ModLoaderType>,
    val releaseChannel: ProjectReleaseChannel,
    val size: Long,
    val sha1: String?,
    val md5: String? = null,
    val publishedAt: Instant,
    val dependencies: List<ProjectId>,
    /** Set only when this file can't be downloaded automatically (e.g. its author disabled distribution) — the page a user can download it from manually. */
    val manualDownloadUrl: String? = null
)

enum class ProjectReleaseChannel {
    Release,
    Beta,
    Alpha
}
