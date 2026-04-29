package app.vercors.feature.project.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProjectPlatformData(
    val id: String,
    val name: String,
    val baseUrl: String,
    val categories: Map<ProjectTypeData, Map<String, List<ProjectCategoryData>>>
)
