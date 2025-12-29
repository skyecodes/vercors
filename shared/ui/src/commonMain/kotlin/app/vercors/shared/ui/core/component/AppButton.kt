package app.vercors.shared.ui.core.component

import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.vercors.shared.ui.core.theme.button
import app.vercors.shared.ui.core.theme.textStyles
import com.composeunstyled.Button
import com.composeunstyled.LocalContentColor
import com.composeunstyled.ProvideTextStyle
import com.composeunstyled.theme.Theme

@Composable
fun AppButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(4.dp),
    backgroundColor: Color = Color.Unspecified,
    contentColor: Color = LocalContentColor.current,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
    borderColor: Color = Color.Unspecified,
    borderWidth: Dp = 0.dp,
    modifier: Modifier = Modifier,
    role: Role = Role.Button,
    indication: Indication? = LocalIndication.current,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    textStyle: TextStyle = Theme[textStyles][button],
    content: @Composable (RowScope.() -> Unit)
) {
    val hovered by interactionSource.collectIsHoveredAsState()

    ProvideTextStyle(textStyle) {
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = shape,
            backgroundColor = if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.5f),
            contentColor = if (enabled) contentColor else contentColor.copy(alpha = 0.8f),
            contentPadding = contentPadding,
            borderColor = borderColor,
            borderWidth = borderWidth,
            modifier = modifier.then(if (hovered) Modifier.pointerHoverIcon(PointerIcon.Hand, true) else Modifier),
            role = role,
            indication = indication,
            interactionSource = interactionSource,
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment,
            content = content
        )
    }
}

val ThinButtonContentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)

val NormalButtonContentPadding = PaddingValues(horizontal = 8.dp, vertical = 7.dp)

@Composable
fun AppIconTextButton(
    onClick: () -> Unit,
    text: String? = null,
    imageVector: ImageVector? = null,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(4.dp),
    backgroundColor: Color = Color.Unspecified,
    contentColor: Color = LocalContentColor.current,
    contentPadding: PaddingValues = NormalButtonContentPadding,
    borderColor: Color = Color.Unspecified,
    borderWidth: Dp = 0.dp,
    modifier: Modifier = Modifier,
    role: Role = Role.Button,
    indication: Indication? = LocalIndication.current,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    textStyle: TextStyle = Theme[textStyles][button],
    iconSize: Dp = 16.dp
) {
    AppButton(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        contentPadding = contentPadding,
        borderColor = borderColor,
        borderWidth = borderWidth,
        modifier = modifier,
        role = role,
        indication = indication,
        interactionSource = interactionSource,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        textStyle = textStyle
    ) {
        imageVector?.let {
            AppIcon(
                imageVector = it,
                contentDescription = text,
                modifier = Modifier.size(iconSize)
            )
        }
        text?.let {
            AppText(
                text = it
            )
        }
    }
}

@Composable
fun AppIconTextButton(
    onClick: () -> Unit,
    text: String? = null,
    painter: Painter? = null,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(4.dp),
    backgroundColor: Color = Color.Unspecified,
    contentColor: Color = LocalContentColor.current,
    contentPadding: PaddingValues = NormalButtonContentPadding,
    borderColor: Color = Color.Unspecified,
    borderWidth: Dp = 0.dp,
    modifier: Modifier = Modifier,
    role: Role = Role.Button,
    indication: Indication? = LocalIndication.current,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    textStyle: TextStyle = Theme[textStyles][button],
    iconSize: Dp = 16.dp
) {
    AppButton(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        contentPadding = contentPadding,
        borderColor = borderColor,
        borderWidth = borderWidth,
        modifier = modifier,
        role = role,
        indication = indication,
        interactionSource = interactionSource,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        textStyle = textStyle
    ) {
        painter?.let {
            AppIcon(
                painter = it,
                contentDescription = text,
                modifier = Modifier.size(iconSize)
            )
        }
        text?.let {
            AppText(
                text = it
            )
        }
    }
}
