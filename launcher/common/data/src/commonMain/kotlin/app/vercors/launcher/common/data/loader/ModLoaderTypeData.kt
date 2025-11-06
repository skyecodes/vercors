package app.vercors.launcher.common.data.loader

import app.vercors.launcher.common.domain.loader.ModLoaderType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ModLoaderTypeData {
    @SerialName("neoforge")
    Neoforge,

    @SerialName("forge")
    Forge,

    @SerialName("fabric")
    Fabric,

    @SerialName("quilt")
    Quilt
}

fun ModLoaderTypeData.toDomain(): ModLoaderType {
    return when (this) {
        ModLoaderTypeData.Neoforge -> ModLoaderType.Neoforge
        ModLoaderTypeData.Forge -> ModLoaderType.Forge
        ModLoaderTypeData.Fabric -> ModLoaderType.Fabric
        ModLoaderTypeData.Quilt -> ModLoaderType.Quilt
    }
}

fun ModLoaderType.toData(): ModLoaderTypeData {
    return when (this) {
        ModLoaderType.Neoforge -> ModLoaderTypeData.Neoforge
        ModLoaderType.Forge -> ModLoaderTypeData.Forge
        ModLoaderType.Fabric -> ModLoaderTypeData.Fabric
        ModLoaderType.Quilt -> ModLoaderTypeData.Quilt
    }
}