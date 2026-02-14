package app.vercors.feature.project.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProjectSearchResponseData(
    val results: List<BriefProjectData>,
    val total: Int
)
