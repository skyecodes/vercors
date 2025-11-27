package app.vercors.meta.lib.game

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class GameVersionType {
    @SerialName("release")
    Release,

    @SerialName("snapshot")
    Snapshot,

    @SerialName("beta")
    Beta,

    @SerialName("alpha")
    Alpha,
}
