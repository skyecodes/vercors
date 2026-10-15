package app.vercors.core.ui.component

import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.theme.button
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.surface2
import app.vercors.core.ui.theme.textStyles
import com.composeunstyled.LocalContentColor
import com.composeunstyled.ProvideContentColor
import com.composeunstyled.ProvideTextStyle
import com.composeunstyled.UnstyledButton
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
    ProvideTextStyle(textStyle) {
        ProvideContentColor(if (enabled) contentColor else contentColor.copy(alpha = 0.8f)) {
            UnstyledButton(
                onClick = onClick,
                enabled = enabled,
                contentPadding = contentPadding,
                modifier = modifier
                    .clip(shape)
                    .background(if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.5f))
                    .border(borderWidth, borderColor, shape)
                    .then(if (enabled) Modifier.pointerHoverIcon(PointerIcon.Hand, true) else Modifier)
                    .width(IntrinsicSize.Max),
                role = role,
                indication = indication,
                interactionSource = interactionSource,
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = horizontalArrangement,
                    verticalAlignment = verticalAlignment,
                    content = content
                )
            }
        }
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
    backgroundColor: Color = Theme[colors][surface2],
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
        AppIconTextButtonContent(
            text = text,
            imageVector = imageVector,
            iconSize = iconSize
        )
    }
}

@Composable
fun AppIconTextButtonContent(
    text: String?,
    imageVector: ImageVector?,
    iconSize: Dp = 16.dp
) {
    imageVector?.let {
        AppIcon(
            imageVector = it,
            contentDescription = text,
            modifier = Modifier.let { modifier ->
                if (text == null) modifier.padding(vertical = 1.dp) else modifier
            }.size(iconSize)
        )
    }
    text?.let {
        AppText(
            text = it,
            singleLine = true,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AppIconTextButtonContent(
    text: String?,
    painter: Painter?,
    iconSize: Dp = 16.dp
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

@Composable
fun AppIconTextButton(
    onClick: () -> Unit,
    text: String? = null,
    painter: Painter? = null,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(4.dp),
    backgroundColor: Color = Theme[colors][surface2],
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
        AppIconTextButtonContent(
            text = text,
            painter = painter,
            iconSize = iconSize
        )
    }
}
