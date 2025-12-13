package app.vercors.meta.lib.game

import kotlinx.serialization.Serializable

@Serializable
data class BriefGameVersionData(
    val id: String,
    val type: GameVersionTypeData,
)
