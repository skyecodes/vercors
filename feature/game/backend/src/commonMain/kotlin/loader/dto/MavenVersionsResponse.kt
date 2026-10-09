package app.vercors.feature.game.backend.loader.dto

import kotlinx.serialization.Serializable

@Serializable
data class MavenVersionsResponse(val versions: List<String>)
