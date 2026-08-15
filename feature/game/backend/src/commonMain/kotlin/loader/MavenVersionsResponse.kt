package app.vercors.feature.game.backend.loader

import kotlinx.serialization.Serializable

@Serializable
data class MavenVersionsResponse(val versions: List<String>)
