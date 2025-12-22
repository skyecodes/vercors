package app.vercors.meta.lib.project

import kotlinx.serialization.Serializable

@Serializable
data class ProjectCategoryData(
    val id: String,
    val slug: String,
    val name: String?,
    val icon: IconData?,
)
