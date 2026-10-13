package app.vercors.feature.project.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProjectFileMatchData(
    val sha1: String,
    val projectId: String,
    val file: ProjectFileData
)
