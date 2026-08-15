package app.vercors.feature.game.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoaderVersionData(
    val version: String,
    val stable: Boolean,
)
