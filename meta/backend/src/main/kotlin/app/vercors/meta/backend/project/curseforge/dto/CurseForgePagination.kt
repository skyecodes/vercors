package app.vercors.meta.backend.project.curseforge.dto

import kotlinx.serialization.Serializable

@Serializable
data class CurseForgePagination(
    val index: Int,
    val pageSize: Int,
    val resultCount: Int,
    val totalCount: Int
)