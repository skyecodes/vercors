package app.vercors.feature.project.backend.curseforge.dto

import kotlinx.serialization.Serializable

@Serializable
data class CurseForgeProjectSearchResponse(
    val data: List<CurseForgeProject>,
    val pagination: CurseForgePagination
)
