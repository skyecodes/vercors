package app.vercors.feature.project.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ProjectTypeData {
    @SerialName("mod")
    Mod,
    @SerialName("modpack")
    Modpack,
    @SerialName("resourcepack")
    ResourcePack,
    @SerialName("shaderpack")
    ShaderPack,
    @SerialName("datapack")
    DataPack,
    @SerialName("world")
    World
}
