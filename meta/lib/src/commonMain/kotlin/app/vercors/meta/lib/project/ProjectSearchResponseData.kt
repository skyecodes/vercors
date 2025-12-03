package app.vercors.meta.lib.project

import kotlinx.serialization.Serializable

@Serializable
data class ProjectSearchResponseData(
    val results: List<ProjectData>
)