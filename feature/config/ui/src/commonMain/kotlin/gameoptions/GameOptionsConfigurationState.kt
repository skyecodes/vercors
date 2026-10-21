package app.vercors.feature.config.ui.gameoptions

import androidx.compose.runtime.Immutable
import app.vercors.feature.instance.domain.model.options.DefaultOptions
import app.vercors.feature.instance.domain.model.options.OptionsCategory
import kotlin.time.Instant

@Immutable
data class GameOptionsConfigurationState(
    val defaultOptions: DefaultOptionsView?,
    val promptClearDefaultOptions: Boolean,
)

@Immutable
data class DefaultOptionsView(
    val sourceInstanceName: String,
    val gameVersion: String,
    val capturedAt: Instant,
    val categories: List<OptionsCategory>,
    val autoApply: Boolean,
)

fun DefaultOptions.toView(): DefaultOptionsView = DefaultOptionsView(
    sourceInstanceName = sourceInstanceName,
    gameVersion = gameVersion,
    capturedAt = capturedAt,
    categories = categories.sorted(),
    autoApply = autoApply,
)
