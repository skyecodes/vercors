package app.vercors.core.ui.component

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.primary
import app.vercors.core.ui.theme.surface
import com.composables.core.androidx.annotation.FloatRange
import com.composeunstyled.ProgressBar
import com.composeunstyled.ProgressIndicator
import com.composeunstyled.theme.Theme

@Composable
fun AppProgressIndicator(
    @FloatRange(from = 0.0, to = 1.0) progress: Float,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(4.dp),
    backgroundColor: Color = Theme[colors][surface],
    contentColor: Color = Theme[colors][primary]
) {
    ProgressIndicator(
        progress = progress,
        modifier = modifier.heightIn(max = 8.dp),
        shape = shape,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
    ) {
        ProgressBar()
    }
}
