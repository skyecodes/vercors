package app.vercors.feature.instance.data.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class MmcPack(val components: List<MmcPackComponent> = emptyList())

@Serializable
internal data class MmcPackComponent(val uid: String, val version: String? = null)
