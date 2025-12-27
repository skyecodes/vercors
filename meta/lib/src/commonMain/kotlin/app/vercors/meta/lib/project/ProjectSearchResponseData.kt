package app.vercors.meta.lib.project

import kotlinx.serialization.Serializable

@Serializable
data class ProjectSearchResponseData(
    val results: List<BriefProjectData>,
    val total: Int
)
