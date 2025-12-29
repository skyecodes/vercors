package app.vercors.shared.ui.core.modifier

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import app.vercors.shared.ui.core.theme.colors
import app.vercors.shared.ui.core.theme.surface
import com.composeunstyled.theme.Theme

private const val gradientSize = 64f

@Composable
fun Modifier.scrollGradient(
    listState: LazyListState,
    orientation: Orientation,
    color: Color = Theme[colors][surface]
): Modifier = drawWithContent {
    drawContent()
    if (listState.canScrollBackward) drawGradient(
        orientation = orientation,
        colors = listOf(color, Color.Transparent),
        start = 0f,
        end = gradientSize
    )
    if (listState.canScrollForward) {
        val sizeOnAxis = when (orientation) {
            Orientation.Vertical -> size.height
            Orientation.Horizontal -> size.width
        }
        drawGradient(
            orientation = orientation,
            colors = listOf(Color.Transparent, color),
            start = sizeOnAxis - gradientSize,
            end = sizeOnAxis
        )
    }
}

private fun DrawScope.drawGradient(
    orientation: Orientation,
    colors: List<Color>,
    start: Float,
    end: Float
) = drawRect(
    when (orientation) {
        Orientation.Vertical -> Brush.verticalGradient(colors = colors, startY = start, endY = end)
        Orientation.Horizontal -> Brush.horizontalGradient(colors = colors, startX = start, endX = end)
    }
)
