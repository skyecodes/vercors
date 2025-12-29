package app.vercors.launcher.gui.project

import app.vercors.shared.domain.project.model.BriefProject
import app.vercors.shared.domain.project.model.ProjectId
import nl.jacobras.humanreadable.HumanReadable
import kotlin.time.Instant

data class BriefProjectView(
    val id: ProjectId,
    val name: String,
    val description: String,
    val author: String,
    val url: String,
    val downloads: Long,
    val lastUpdated: Instant,
    val logoUrl: String?,
    val imageUrl: String?
) {
    val downloadsReadable: String = HumanReadable.abbreviation(downloads, decimals = if (downloads > 1000) 1 else 0)
    val lastUpdatedReadable: String = HumanReadable.timeAgo(lastUpdated)
}

fun BriefProject.toView(): BriefProjectView = BriefProjectView(
    id = id,
    name = name,
    description = description,
    author = author,
    url = url,
    downloads = downloads,
    lastUpdated = lastUpdated,
    logoUrl = logoUrl,
    imageUrl = imageUrl
)
