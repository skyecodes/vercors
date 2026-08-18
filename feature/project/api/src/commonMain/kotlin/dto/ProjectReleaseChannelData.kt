package app.vercors.feature.project.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ProjectReleaseChannelData {
    @SerialName("release")
    Release,

    @SerialName("beta")
    Beta,

    @SerialName("alpha")
    Alpha
}
