package app.vercors.feature.project.backend.curseforge.dto

import kotlinx.serialization.Serializable

@Serializable
data class CurseForgeStandardResponse<T>(
    val data: T
)
