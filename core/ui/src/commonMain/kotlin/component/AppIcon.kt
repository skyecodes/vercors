package app.vercors.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import com.composeunstyled.LocalContentColor
import com.composeunstyled.UnstyledIcon

@Composable
fun AppIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    UnstyledIcon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}

@Composable
fun AppIcon(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    UnstyledIcon(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}
