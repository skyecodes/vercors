package app.vercors.feature.project.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ProjectSortByData {
    @SerialName("relevance")
    Relevance,

    @SerialName("downloads")
    Downloads,

    @SerialName("updated")
    DateUpdated,

    @SerialName("created")
    DateCreated
}
