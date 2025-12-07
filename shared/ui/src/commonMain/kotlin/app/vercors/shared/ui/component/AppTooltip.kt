package app.vercors.shared.ui.component

import androidx.compose.runtime.Composable
import com.composeunstyled.RelativeAlignment
import com.composeunstyled.Tooltip

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
