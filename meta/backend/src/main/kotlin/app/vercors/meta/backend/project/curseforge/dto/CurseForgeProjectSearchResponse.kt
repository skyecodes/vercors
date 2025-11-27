package app.vercors.meta.backend.project.curseforge.dto

import kotlinx.serialization.Serializable

@Serializable
data class CurseForgeProjectSearchResponse(
    val data: List<CurseForgeProject>,
    val pagination: CurseForgePagination
)