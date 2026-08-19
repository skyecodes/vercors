package app.vercors.core.ui.component

import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.animation.AppAnimatedVisibility
import app.vercors.core.ui.animation.appAnimateColorAsState
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.onPrimary
import app.vercors.core.ui.theme.primary
import app.vercors.core.ui.theme.surface2
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
    uncheckedBackgroundColor: Color = Theme[colors][surface2],
    checkedBackgroundColor: Color = Theme[colors][primary],
    contentColor: Color = Theme[colors][onPrimary],
    checkboxSize: Dp = 24.dp,
    onCheckedChange: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.pointerHoverIcon(PointerIcon.Hand).clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = { onCheckedChange(!checked) }
        )
    ) {
        val backgroundColor by appAnimateColorAsState(if (checked) checkedBackgroundColor else uncheckedBackgroundColor)

        if (label != null && !invert) AppText(label)
        UnstyledCheckbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(backgroundColor)
                .size(checkboxSize),
            interactionSource = interactionSource,
            indication = LocalIndication.current
        ) {
            CheckIcon(
                checked = checked,
                checkboxSize = checkboxSize,
                contentColor = contentColor
            )
        }
        if (label != null && invert) AppText(label)
    }
}

@Composable
private fun CheckIcon(
    checked: Boolean,
    checkboxSize: Dp,
    contentColor: Color
) {
    AppAnimatedVisibility(
        visible = checked,
        enter = fadeIn(spring()),
        exit = fadeOut(spring())
    ) {
        AppIcon(
            modifier = Modifier.size(checkboxSize).padding(2.dp),
            imageVector = Lucide.Check,
            contentDescription = null,
            tint = contentColor,
        )
    }
}
