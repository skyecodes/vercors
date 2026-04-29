package app.vercors.feature.project.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ProjectLoaderData {
    @SerialName("forge")
    Forge,

    @SerialName("neoforge")
    NeoForge,

    @SerialName("fabric")
    Fabric,

    @SerialName("quilt")
    Quilt
}
