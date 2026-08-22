package app.vercors.shared.data.dto

import app.vercors.shared.domain.model.JvmMemoryOptions
import kotlinx.serialization.Serializable

@Serializable
data class JvmMemoryOptionsData(
    val minMemoryMb: Int? = null,
    val maxMemoryMb: Int? = null,
    val extraArgs: String = "",
)

fun JvmMemoryOptionsData.toDomain(): JvmMemoryOptions = JvmMemoryOptions(
    minMemoryMb = minMemoryMb,
    maxMemoryMb = maxMemoryMb,
    extraArgs = extraArgs,
)

fun JvmMemoryOptions.toData(): JvmMemoryOptionsData = JvmMemoryOptionsData(
    minMemoryMb = minMemoryMb,
    maxMemoryMb = maxMemoryMb,
    extraArgs = extraArgs,
)
