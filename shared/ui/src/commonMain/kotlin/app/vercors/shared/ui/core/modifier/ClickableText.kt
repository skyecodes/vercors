package app.vercors.shared.ui.core.modifier

import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon

fun Modifier.clickableText(onClick: () -> Unit) =
    clickable(interactionSource = null, indication = null) { onClick() }.pointerHoverIcon(PointerIcon.Hand)
