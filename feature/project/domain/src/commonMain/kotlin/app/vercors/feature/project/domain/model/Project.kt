package app.vercors.feature.project.domain.model

import kotlin.time.Instant

data class Project(
    val id: ProjectId,
    val name: String,
    val description: String,
    val body: String?,
    val url: String,
    val logoUrl: String?,
)

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

data class ProjectId(
    val id: String,
    val platform: PlatformId
)

infix fun String.from(platform: PlatformId) = ProjectId(this, platform)
