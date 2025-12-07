package app.vercors.launcher.common.data.platform

import app.vercors.shared.domain.project.model.ProjectPlatform
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PlatformTypeData {
    @SerialName("modrinth")
    Modrinth,

    @SerialName("curseforge")
    CurseForge
}

fun PlatformTypeData.toDomain(): ProjectPlatform {
    return when (this) {
        PlatformTypeData.Modrinth -> ProjectPlatform.Modrinth
        PlatformTypeData.CurseForge -> ProjectPlatform.CurseForge
    }
}

fun ProjectPlatform.toData(): PlatformTypeData {
    return when (this) {
        ProjectPlatform.Modrinth -> PlatformTypeData.Modrinth
        ProjectPlatform.CurseForge -> PlatformTypeData.CurseForge
    }
}
