package app.vercors.shared.ui.core.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode.Reverse
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import app.vercors.shared.ui.core.animation.appAnimateFloat
import app.vercors.shared.ui.core.theme.background
import app.vercors.shared.ui.core.theme.colors
import com.composeunstyled.LocalTextStyle
import com.composeunstyled.theme.Theme

@Composable
fun AppLoadingPlaceholder(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Theme[colors][background]
) {
    val infiniteTransition = rememberInfiniteTransition()
    val float by infiniteTransition.appAnimateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = Reverse
        )
    )

    Box(modifier = modifier.background(backgroundColor.copy(alpha = float), RoundedCornerShape(4.dp)))
}

@Composable
fun AppLoadingTextPlaceholder(
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    backgroundColor: Color = Theme[colors][background]
) {
    val padding = 2.dp
    val textHeight = textStyle.fontSize.value.dp + padding * 2

    AppLoadingPlaceholder(
        modifier = modifier.height(height = textHeight).padding(vertical = padding),
        backgroundColor = backgroundColor
    )
}
