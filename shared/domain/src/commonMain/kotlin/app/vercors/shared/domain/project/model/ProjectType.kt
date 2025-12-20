package app.vercors.shared.domain.project.model

import kotlinx.serialization.Serializable

@Serializable
enum class ProjectType {
    Mod,
    Modpack,
    ResourcePack,
    ShaderPack,
    DataPack,
    World,
}
