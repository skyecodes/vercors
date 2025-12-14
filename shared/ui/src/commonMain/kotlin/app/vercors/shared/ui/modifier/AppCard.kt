package app.vercors.shared.ui.modifier

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import app.vercors.shared.ui.colors
import app.vercors.shared.ui.surface
import com.composeunstyled.theme.Theme

@Composable
fun Modifier.appCard(
    interactionSource: MutableInteractionSource? = null,
    backgroundColor: Color = Theme[colors][surface],
    shape: Shape = RoundedCornerShape(8.dp),
    onClick: (() -> Unit)? = null
) = this
    .background(backgroundColor, shape)
    .clip(shape) then
        if (onClick != null)
            Modifier.pointerHoverIcon(PointerIcon.Hand)
                .clickable(interactionSource = interactionSource, onClick = onClick)
        else Modifier
