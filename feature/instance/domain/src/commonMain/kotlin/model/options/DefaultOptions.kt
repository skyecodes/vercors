package app.vercors.feature.instance.domain.model.options

import kotlin.time.Instant

data class DefaultOptions(
    val categories: Set<OptionsCategory>,
    val sourceInstanceName: String,
    val gameVersion: String,
    val capturedAt: Instant,
    val autoApply: Boolean = true,
)
