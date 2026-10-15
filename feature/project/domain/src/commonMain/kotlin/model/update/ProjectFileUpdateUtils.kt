package app.vercors.feature.project.domain.model.update

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
