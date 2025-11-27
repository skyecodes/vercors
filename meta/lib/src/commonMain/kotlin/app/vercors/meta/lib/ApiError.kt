package app.vercors.meta.lib

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    val message: String
)
