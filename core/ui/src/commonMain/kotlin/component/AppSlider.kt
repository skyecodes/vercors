package app.vercors.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.primary
import app.vercors.core.ui.theme.surface2
import com.composeunstyled.SliderState
import com.composeunstyled.UnstyledSlider
import com.composeunstyled.theme.Theme
import kotlin.math.roundToInt

private const val DISABLED_ALPHA = 0.6f

/** A RAM/MB-style slider: [value] snaps to the nearest multiple of [stepSize] within [valueRange]. Since
 *  [valueRange]'s span is rarely an exact multiple of [stepSize] (e.g. total system RAM), the usable upper bound is
 *  floor-aligned to a [stepSize] multiple from the lower bound — otherwise the top tick would be unreachable. */
@Composable
fun AppSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: IntRange,
    stepSize: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onValueChangeFinished: (() -> Unit)? = null,
    trackColor: Color = Theme[colors][surface2],
    fillColor: Color = Theme[colors][primary],
) {
    require(stepSize > 0) { "stepSize must be > 0" }
    val alignedSpan = ((valueRange.last - valueRange.first) / stepSize) * stepSize
    val steps = ((alignedSpan / stepSize) - 1).coerceAtLeast(0)
    val floatRange = valueRange.first.toFloat()..(valueRange.first + alignedSpan).toFloat()

    UnstyledSlider(
        value = value.toFloat().coerceIn(floatRange),
        onValueChange = { onValueChange(it.roundToInt()) },
        modifier = modifier,
        enabled = enabled,
        valueRange = floatRange,
        steps = steps,
        onValueChangeFinished = onValueChangeFinished,
        track = { state -> AppSliderTrack(state, enabled, trackColor, fillColor) },
        thumb = { AppSliderThumb(enabled, fillColor) },
    )
}

@Composable
private fun AppSliderTrack(state: SliderState, enabled: Boolean, trackColor: Color, fillColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .clip(RoundedCornerShape(2.dp))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(state.fraction)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(fillColor)
        )
    }
}

@Composable
private fun AppSliderThumb(enabled: Boolean, color: Color) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .clip(CircleShape)
            .background(color)
    )
}
