package app.vercors.meta.backend.project.curseforge.dto

import kotlinx.serialization.SerialName

enum class CurseForgeSortOrder {
    @SerialName("asc")
    Asc,

    @SerialName("desc")
    Desc
}