package app.vercors.shared.data.dto

import app.vercors.shared.domain.model.JvmMemoryOptions
import kotlinx.serialization.Serializable

@Serializable
data class JvmMemoryOptionsData(
    val minMemoryEnabled: Boolean? = null,
    val minMemoryMb: Int? = null,
    val maxMemoryEnabled: Boolean? = null,
    val maxMemoryMb: Int? = null,
    val extraArgs: String? = null,
)

fun JvmMemoryOptionsData.toDomain(): JvmMemoryOptions = JvmMemoryOptions(
    minMemoryEnabled = minMemoryEnabled,
    minMemoryMb = minMemoryMb,
    maxMemoryEnabled = maxMemoryEnabled,
    maxMemoryMb = maxMemoryMb,
    extraArgs = extraArgs,
)

fun JvmMemoryOptions.toData(): JvmMemoryOptionsData = JvmMemoryOptionsData(
    minMemoryEnabled = minMemoryEnabled,
    minMemoryMb = minMemoryMb,
    maxMemoryEnabled = maxMemoryEnabled,
    maxMemoryMb = maxMemoryMb,
    extraArgs = extraArgs,
)
