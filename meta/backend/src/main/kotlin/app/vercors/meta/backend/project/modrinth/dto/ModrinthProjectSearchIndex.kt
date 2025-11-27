package app.vercors.meta.backend.project.modrinth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ModrinthProjectSearchIndex {
    @SerialName("relevance")
    Relevance,

    @SerialName("downloads")
    Downloads,

    @SerialName("follows")
    Follows,

    @SerialName("newest")
    Newest,

    @SerialName("updated")
    Updated
}
