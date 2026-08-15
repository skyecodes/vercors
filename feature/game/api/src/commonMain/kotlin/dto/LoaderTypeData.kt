package app.vercors.feature.game.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class LoaderTypeData {
    @SerialName("fabric")
    Fabric,

    @SerialName("quilt")
    Quilt,

    @SerialName("forge")
    Forge,

    @SerialName("neoforge")
    NeoForge
}
