package app.vercors.core.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.surface2
import com.composeunstyled.theme.Theme

private const val DISABLED_ALPHA = 0.6f

@Composable
fun AppNumberField(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: IntRange? = null,
    enabled: Boolean = true,
    debounceMillis: Long = 500L,
    width: Dp = 72.dp,
    backgroundColor: Color = Theme[colors][surface2],
) {
    // AppTextField sizes itself via Modifier.width(IntrinsicSize.Max) internally, which doesn't play
    // well when placed next to a weight(1f) sibling in a Row (it can claim far more width than intended).
    // A Box with a fixed width isolates it from that, giving this field a stable, predictable footprint.
    Box(modifier = modifier.width(width)) {
        AppTextField(
            initialValue = value.toString(),
            onValueChange = { text ->
                text.toIntOrNull()?.let { parsed ->
                    onValueChange(valueRange?.let { parsed.coerceIn(it) } ?: parsed)
                }
            },
            debounceMillis = debounceMillis,
            enabled = enabled,
            backgroundColor = backgroundColor,
            modifier = Modifier.fillMaxWidth().alpha(if (enabled) 1f else DISABLED_ALPHA),
        )
    }
}
