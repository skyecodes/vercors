package app.vercors.feature.instance.data.dto

import app.vercors.feature.instance.domain.model.options.DefaultOptions
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class DefaultOptionsData(
    val categories: List<OptionsCategoryData>,
    val sourceInstanceName: String,
    val gameVersion: String,
    val capturedAt: Instant,
    val autoApply: Boolean = true,
)

fun DefaultOptionsData.toDomain(): DefaultOptions = DefaultOptions(
    categories = categories.mapTo(mutableSetOf()) { it.toDomain() },
    sourceInstanceName = sourceInstanceName,
    gameVersion = gameVersion,
    capturedAt = capturedAt,
    autoApply = autoApply,
)

fun DefaultOptions.toData(): DefaultOptionsData = DefaultOptionsData(
    categories = categories.sorted().map { it.toData() },
    sourceInstanceName = sourceInstanceName,
    gameVersion = gameVersion,
    capturedAt = capturedAt,
    autoApply = autoApply,
)
