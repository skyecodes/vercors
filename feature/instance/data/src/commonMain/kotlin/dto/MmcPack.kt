package app.vercors.feature.instance.data.dto

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

@Serializable
internal data class MmcPack(
    val components: List<MmcPackComponent> = emptyList(),
    @EncodeDefault val formatVersion: Int = 1
)

@Serializable
internal data class MmcPackComponent(
    val uid: String,
    val version: String? = null,
    val important: Boolean = false,
    val dependencyOnly: Boolean = false
)
