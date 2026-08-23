package app.vercors.feature.game.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoaderErrorData(
    val error: String,
    val description: String? = null
)
