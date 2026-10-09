package app.vercors.core.ui.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha

private const val DISABLED_ALPHA = 0.6f

fun Modifier.disabledAlpha(enabled: Boolean): Modifier = if (enabled) this else alpha(DISABLED_ALPHA)
