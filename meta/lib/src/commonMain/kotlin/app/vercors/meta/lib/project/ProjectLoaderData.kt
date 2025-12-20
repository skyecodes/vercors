package app.vercors.meta.lib.project

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ProjectLoaderData {
    @SerialName("forge")
    Forge,

    @SerialName("neoforge")
    Neoforge,

    @SerialName("fabric")
    Fabric,

    @SerialName("quilt")
    Quilt
}
