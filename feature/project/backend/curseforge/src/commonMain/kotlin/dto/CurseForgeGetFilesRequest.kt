package app.vercors.feature.project.backend.curseforge.dto

import kotlinx.serialization.Serializable

@Serializable
data class CurseForgeGetFilesRequest(
    val fileIds: List<Int>
)
