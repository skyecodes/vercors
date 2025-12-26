package app.vercors.shared.data.loader

import app.vercors.shared.domain.loader.ModLoaderType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ModLoaderTypeData {
    @SerialName("neoforge")
    NeoForge,

    @SerialName("forge")
    Forge,

    @SerialName("fabric")
    Fabric,

    @SerialName("quilt")
    Quilt
}

fun ModLoaderTypeData.toDomain(): ModLoaderType {
    return when (this) {
        ModLoaderTypeData.NeoForge -> ModLoaderType.NeoForge
        ModLoaderTypeData.Forge -> ModLoaderType.Forge
        ModLoaderTypeData.Fabric -> ModLoaderType.Fabric
        ModLoaderTypeData.Quilt -> ModLoaderType.Quilt
    }
}

fun ModLoaderType.toData(): ModLoaderTypeData {
    return when (this) {
        ModLoaderType.NeoForge -> ModLoaderTypeData.NeoForge
        ModLoaderType.Forge -> ModLoaderTypeData.Forge
        ModLoaderType.Fabric -> ModLoaderTypeData.Fabric
        ModLoaderType.Quilt -> ModLoaderTypeData.Quilt
    }
}
