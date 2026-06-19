package app.vercors.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.onBackground
import app.vercors.core.ui.theme.surface
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.composeunstyled.UnstyledCheckbox
import com.composeunstyled.theme.Theme

@Composable
fun AppCheckbox(
    label: String? = null,
    checked: Boolean,
    invert: Boolean = false,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Theme[colors][surface],
    contentColor: Color = Theme[colors][onBackground],
    checkboxSize: Dp = 24.dp,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.pointerHoverIcon(PointerIcon.Hand).clickable(
            interactionSource = null,
            indication = null,
            onClick = { onCheckedChange(!checked) }
        )
    ) {
        if (label != null && !invert) AppText(label)
        UnstyledCheckbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(backgroundColor)
                .size(checkboxSize)
        ) {
            if (checked) {
                AppIcon(
                    modifier = Modifier.size(checkboxSize).padding(4.dp),
                    imageVector = Lucide.Check,
                    contentDescription = null,
                    tint = contentColor,
                )
            }
        }
        if (label != null && invert) AppText(label)
    }
}
