package app.vercors.meta.backend.project.curseforge.dto

import kotlinx.serialization.Serializable

@Serializable
data class CurseForgeCategoriesResponse(
    val data: List<CurseForgeCategory>
)
