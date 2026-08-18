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
    val publishedAt: Instant
)

enum class ProjectReleaseChannel {
    Release,
    Beta,
    Alpha
}
