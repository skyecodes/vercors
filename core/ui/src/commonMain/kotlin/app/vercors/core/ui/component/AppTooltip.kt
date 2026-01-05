package app.vercors.core.ui.component

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.surface2
import com.composeunstyled.LocalContentColor
import com.composeunstyled.RelativeAlignment
import com.composeunstyled.Tooltip
import com.composeunstyled.TooltipPanel
import com.composeunstyled.theme.Theme

@Composable
fun AppTooltip(
    enabled: Boolean = true,
    panel: @Composable () -> Unit,
    placement: RelativeAlignment = RelativeAlignment.TopCenter,
    longPressShowDurationMillis: Long = 1500L,
    hoverDelayMillis: Long = 0L,
    anchor: @Composable () -> Unit
) {
    Tooltip(
        enabled = enabled,
        panel = panel,
        placement = placement,
        longPressShowDurationMillis = longPressShowDurationMillis,
        hoverDelayMillis = hoverDelayMillis,
        anchor = anchor
    )
}

@Composable
fun AppTooltipPanel(
    modifier: Modifier = Modifier,
    enter: EnterTransition = EnterTransition.None,
    exit: ExitTransition = ExitTransition.None,
    shape: Shape = RoundedCornerShape(4.dp),
    backgroundColor: Color = Theme[colors][surface2],
    contentColor: Color = LocalContentColor.current,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
    content: @Composable () -> Unit
) {
    TooltipPanel(
        modifier = modifier,
        enter = enter,
        exit = exit,
        shape = shape,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        contentPadding = contentPadding,
        content = content
    )
}
