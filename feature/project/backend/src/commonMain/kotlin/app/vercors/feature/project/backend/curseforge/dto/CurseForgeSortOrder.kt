package app.vercors.feature.project.backend.curseforge.dto

import kotlinx.serialization.SerialName

enum class CurseForgeSortOrder {
    @SerialName("asc")
    Asc,

    @SerialName("desc")
    Desc
}
