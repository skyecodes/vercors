package app.vercors.feature.project.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class FileHashesData(
    val sha1: String,
    val fingerprint: Long? = null
)
