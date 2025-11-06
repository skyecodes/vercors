package app.vercors.launcher.common.data.platform

import app.vercors.launcher.common.domain.platform.PlatformType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PlatformTypeData {
    @SerialName("modrinth")
    Modrinth,

    @SerialName("curseforge")
    CurseForge
}

fun PlatformTypeData.toDomain(): PlatformType {
    return when (this) {
        PlatformTypeData.Modrinth -> PlatformType.Modrinth
        PlatformTypeData.CurseForge -> PlatformType.CurseForge
    }
}

fun PlatformType.toData(): PlatformTypeData {
    return when (this) {
        PlatformType.Modrinth -> PlatformTypeData.Modrinth
        PlatformType.CurseForge -> PlatformTypeData.CurseForge
    }
}