package app.vercors.meta.backend.project.curseforge.dto

import kotlinx.serialization.Serializable

@Serializable
data class CurseForgeCategory(
    val classId: Int? = null,
    val dateModified: CurseForgeInstant,
    val displayIndex: Int? = null,
    val gameId: Int,
    val iconUrl: String,
    val id: Int,
    val isClass: Boolean? = null,
    val name: String,
    val parentCategoryId: Int? = null,
    val slug: String,
    val url: String
)