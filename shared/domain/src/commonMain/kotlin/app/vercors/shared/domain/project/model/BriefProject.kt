package app.vercors.shared.domain.project.model

import kotlin.time.Instant

data class BriefProject(
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
