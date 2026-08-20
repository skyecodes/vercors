package app.vercors.feature.project.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProjectData(
    val id: String,
    val platform: String,
    val name: String,
    val type: ProjectTypeData,
    val description: String,
    val body: String? = null,
    val url: String,
    val logoUrl: String? = null,
    val gameVersions: List<String> = emptyList(),
    val loaders: List<ProjectLoaderData> = emptyList(),
    val categories: List<ProjectCategoryData> = emptyList(),
    val authors: List<ProjectAuthorData> = emptyList(),
    val links: ProjectLinksData = ProjectLinksData()
)
