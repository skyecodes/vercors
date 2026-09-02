package app.vercors.core.ui.modifier

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.appMenuShadow(
    elevation: Dp = 4.dp,
    shape: Shape = RoundedCornerShape(8.dp),
) = this.shadow(elevation, shape)
