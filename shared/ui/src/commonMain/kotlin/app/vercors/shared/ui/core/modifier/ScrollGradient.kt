package app.vercors.shared.ui.core.modifier

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import app.vercors.shared.ui.core.theme.colors
import app.vercors.shared.ui.core.theme.surface
import com.composeunstyled.theme.Theme

@Composable
fun Modifier.scrollGradient(
    listState: LazyListState,
    orientation: Orientation,
    color: Color = Theme[colors][surface]
): Modifier = drawWithContent {
    drawContent()
    val gradients = mutableListOf<Array<Pair<Float, Color>>>()
    if (listState.canScrollBackward) {
        gradients += arrayOf(0f to color, 0.15f to Color.Transparent, 1f to Color.Transparent)
    }
    if (listState.canScrollForward) {
        gradients += arrayOf(0f to Color.Transparent, 0.85f to Color.Transparent, 1f to color)
    }
    gradients.forEach {
        drawRect(
            when (orientation) {
                Orientation.Vertical -> Brush.verticalGradient(*it)
                Orientation.Horizontal -> Brush.horizontalGradient(*it)
            }
        )
    }
}
