package app.vercors.meta.lib.project

import kotlinx.serialization.Serializable

@Serializable
data class PlatformData(
    val id: String,
    val name: String,
    val baseUrl: String,
    val categories: Map<ProjectTypeData, List<ProjectCategoryData>>
)
