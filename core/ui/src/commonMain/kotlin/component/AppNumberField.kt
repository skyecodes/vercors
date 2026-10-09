package app.vercors.core.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.modifier.disabledAlpha
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.surface2
import com.composeunstyled.theme.Theme

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
    // Fixed-width Box: AppTextField's IntrinsicSize.Max width misbehaves next to a weight(1f) sibling
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
            modifier = Modifier.fillMaxWidth().disabledAlpha(enabled),
        )
    }
}
