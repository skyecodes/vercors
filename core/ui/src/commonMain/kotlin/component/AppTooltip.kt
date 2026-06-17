package app.vercors.core.ui.component

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.surface2
import com.composeunstyled.*
import com.composeunstyled.theme.Theme

@Composable
fun AppTooltip(
    enabled: Boolean = true,
    panel: @Composable AppTooltipScope.() -> Unit,
    side: AnchorSide = AnchorSide.Top,
    alignment: AnchorAlignment = AnchorAlignment.Center,
    longPressShowDurationMillis: Long = 1500L,
    hoverDelayMillis: Long = 0L,
    anchor: @Composable () -> Unit
) {
    UnstyledTooltip(
        enabled = enabled,
        panel = {
            with(AppTooltipScope(this)) {
                panel()
            }
        },
        side = side,
        alignment = alignment,
        longPressShowDurationMillis = longPressShowDurationMillis,
        hoverDelayMillis = hoverDelayMillis,
        anchor = anchor
    )
}

@JvmInline
value class AppTooltipScope(val scope: TooltipScope)

@Composable
fun AppTooltipScope.AppTooltipPanel(
    modifier: Modifier = Modifier,
    enter: EnterTransition = EnterTransition.None,
    exit: ExitTransition = ExitTransition.None,
    shape: Shape = RoundedCornerShape(4.dp),
    backgroundColor: Color = Theme[colors][surface2],
    contentColor: Color = LocalContentColor.current,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
    content: @Composable () -> Unit
) {
    scope.TooltipPanel(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .padding(contentPadding),
        enter = enter,
        exit = exit
    ) {
        ProvideContentColor(contentColor) {
            content()
        }
    }
}
