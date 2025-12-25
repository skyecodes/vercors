package app.vercors.launcher.gui.project

import app.vercors.shared.domain.project.model.BriefProject
import app.vercors.shared.domain.project.model.ProjectId
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
)

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
