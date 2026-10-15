package app.vercors.feature.project.domain.model.update

import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.project.domain.model.ProjectFile
import app.vercors.feature.project.domain.model.ProjectReleaseChannel

// A file never offers an update to a less stable channel than its own
fun ProjectReleaseChannel.accepts(other: ProjectReleaseChannel): Boolean = other.ordinal <= ordinal

fun List<ProjectFile>.findUpdateFor(current: ProjectFile): ProjectFile? {
    val candidates = filter {
        it.id != current.id && it.publishedAt > current.publishedAt && current.releaseChannel.accepts(it.releaseChannel)
    }
    return candidates.filter { it.releaseChannel == ProjectReleaseChannel.Release }.maxByOrNull { it.publishedAt }
        ?: candidates.maxByOrNull { it.publishedAt }
}

// Modrinth drops loaders we don't support (bukkit, paper...), so a plugin file comes back with none
fun ProjectFile.runsOn(loader: ModLoaderType): Boolean =
    loaders.any { it == loader || (loader == ModLoaderType.Quilt && it == ModLoaderType.Fabric) }
