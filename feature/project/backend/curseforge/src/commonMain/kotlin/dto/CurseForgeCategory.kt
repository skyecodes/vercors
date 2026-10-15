package app.vercors.feature.project.backend.curseforge.dto

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class CurseForgeCategory(
    val classId: Int? = null,
    val dateModified: Instant,
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
