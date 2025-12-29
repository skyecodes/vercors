package app.vercors.shared.ui.core.component

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import app.vercors.shared.ui.core.theme.colors
import app.vercors.shared.ui.core.theme.primary
import app.vercors.shared.ui.core.theme.surface
import com.composables.core.androidx.annotation.FloatRange
import com.composeunstyled.LocalContentColor
import com.composeunstyled.ProgressBar
import com.composeunstyled.ProgressIndicator
import com.composeunstyled.ProgressIndicatorScope
import com.composeunstyled.theme.Theme

@Composable
fun AppProgressIndicator(
    @FloatRange(from = 0.0, to = 1.0) progress: Float,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(4.dp),
    backgroundColor: Color = Theme[colors][surface],
    contentColor: Color = Theme[colors][primary],
    content: @Composable ProgressIndicatorScope.() -> Unit
) {
    ProgressIndicator(
        progress = progress,
        modifier = modifier.heightIn(max = 8.dp),
        shape = shape,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        content = content
    )
}

@Composable
fun ProgressIndicatorScope.AppProgressBar(
    shape: Shape = RoundedCornerShape(4.dp),
    color: Color = LocalContentColor.current
) {
    ProgressBar(
        shape = shape,
        color = color
    )
}
