package app.vercors.feature.project.backend.modrinth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ModrinthProjectType {
    @SerialName("mod")
    Mod,

    @SerialName("modpack")
    Modpack,

    @SerialName("resourcepack")
    ResourcePack,

    @SerialName("shader")
    ShaderPack,

    @SerialName("datapack")
    DataPack
}
