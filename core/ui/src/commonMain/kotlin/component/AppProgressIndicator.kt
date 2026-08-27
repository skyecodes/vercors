package app.vercors.core.ui.component

import androidx.annotation.FloatRange
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.animation.appAnimateFloatAsState
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.primary
import app.vercors.core.ui.theme.surface2
import com.composeunstyled.Indicator
import com.composeunstyled.UnstyledProgress
import com.composeunstyled.theme.Theme

@Composable
fun AppProgressIndicator(
    @FloatRange(from = 0.0, to = 1.0) progress: Float,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(4.dp),
    backgroundColor: Color = Theme[colors][surface2],
    contentColor: Color = Theme[colors][primary]
) {
    val animatedProgress by appAnimateFloatAsState(targetValue = progress, label = "AppProgressIndicator")

    UnstyledProgress(
        progress = animatedProgress,
        modifier = modifier.heightIn(max = 8.dp)
            .clip(shape)
            .background(backgroundColor)
    ) {
        Indicator(Modifier.background(contentColor))
    }
}
