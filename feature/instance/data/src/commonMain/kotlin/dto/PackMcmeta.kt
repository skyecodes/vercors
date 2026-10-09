package app.vercors.feature.instance.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class PackMcmeta(val pack: PackSection)

@Serializable
internal data class PackSection(
    @SerialName("pack_format") val packFormat: Int = 0,
    val description: JsonElement? = null
)
