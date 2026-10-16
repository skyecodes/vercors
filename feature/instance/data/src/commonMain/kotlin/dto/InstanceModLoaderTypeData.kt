package app.vercors.feature.instance.data.dto

import app.vercors.feature.game.domain.model.ModLoaderType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class InstanceModLoaderTypeData {
    @SerialName("neoforge")
    NeoForge,

    @SerialName("forge")
    Forge,

    @SerialName("fabric")
    Fabric,

    @SerialName("quilt")
    Quilt,
}

fun InstanceModLoaderTypeData.toDomain(): ModLoaderType = when (this) {
    InstanceModLoaderTypeData.NeoForge -> ModLoaderType.NeoForge
    InstanceModLoaderTypeData.Forge -> ModLoaderType.Forge
    InstanceModLoaderTypeData.Fabric -> ModLoaderType.Fabric
    InstanceModLoaderTypeData.Quilt -> ModLoaderType.Quilt
}

fun ModLoaderType.toData(): InstanceModLoaderTypeData = when (this) {
    ModLoaderType.NeoForge -> InstanceModLoaderTypeData.NeoForge
    ModLoaderType.Forge -> InstanceModLoaderTypeData.Forge
    ModLoaderType.Fabric -> InstanceModLoaderTypeData.Fabric
    ModLoaderType.Quilt -> InstanceModLoaderTypeData.Quilt
}
