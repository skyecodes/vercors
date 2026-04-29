package app.vercors.feature.project.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProjectCategoryData(
    val id: String,
    val slug: String,
    val name: String? = null,
    val icon: IconData? = null,
)
