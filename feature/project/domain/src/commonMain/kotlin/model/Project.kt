package app.vercors.feature.project.domain.model

import app.vercors.feature.game.domain.model.ModLoaderType
import kotlin.time.Instant

data class Project(
    val id: ProjectId,
    val name: String,
    val type: ProjectType,
    val description: String,
    val body: String?,
    val url: String,
    val logoUrl: String?,
    val gameVersions: List<String>,
    val loaders: List<ModLoaderType>,
    val categories: List<ProjectCategory>,
    val authors: List<ProjectAuthor>,
    val links: ProjectLinks,
)

data class ProjectAuthor(
    val name: String,
    val url: String?,
    val avatarUrl: String?
)

data class ProjectLinks(
    val websiteUrl: String?,
    val sourceUrl: String?,
    val issuesUrl: String?,
    val wikiUrl: String?,
    val discordUrl: String?
)

data class BriefProject(
    val id: ProjectId,
    val name: String,
    val type: ProjectType,
    val loaders: List<ModLoaderType>,
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
