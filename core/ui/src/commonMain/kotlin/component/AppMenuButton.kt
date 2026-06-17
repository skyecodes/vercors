package app.vercors.core.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.modifier.appCard
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.surface2
import com.composeunstyled.*
import com.composeunstyled.theme.Theme

@Composable
fun AppMenuButton(
    text: String? = null,
    imageVector: ImageVector? = null,
    backgroundColor: Color = Color.Unspecified,
    menuContent: @Composable AppMenuScope.() -> Unit,
    contentPadding: PaddingValues = NormalButtonContentPadding,
    modifier: Modifier = Modifier,
    menuModifier: Modifier = Modifier,
    anchor: AppDropdownPanelAnchor = AppDropdownPanelAnchor.BottomStart
) = with(object : AppMenuScope {
    override var expanded by remember { mutableStateOf(false) }
}) {
    UnstyledDropdownMenu(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = it },
        side = anchor.side,
        alignment = anchor.alignment,
        panel = {
            DropdownMenuPanel(
                modifier = menuModifier.appCard(backgroundColor = Theme[colors][surface2])
                    //.widthIn(min = minWidth, max = maxWidth)
                    .width(IntrinsicSize.Max),
            ) {
                menuContent()
            }
        },
        anchor = {
            AppIconTextButton(
                onClick = { expanded = !expanded },
                backgroundColor = backgroundColor,
                text = text,
                imageVector = imageVector,
                contentPadding = contentPadding,
            )
        }
    )
}

interface AppMenuScope {
    var expanded: Boolean
}

@Composable
fun AppMenuScope.AppMenuItem(
    onClick: () -> Unit,
    text: String? = null,
    imageVector: ImageVector? = null,
    contentColor: Color = LocalContentColor.current
) {
    AppIconTextButton(
        onClick = { onClick(); expanded = false },
        text = text,
        imageVector = imageVector,
        contentColor = contentColor,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        shape = RectangleShape
    )
}

sealed class AppDropdownPanelAnchor(
    val side: AnchorSide,
    val alignment: AnchorAlignment
) {
    data object TopStart : AppDropdownPanelAnchor(AnchorSide.Top, AnchorAlignment.Start)
    data object TopEnd : AppDropdownPanelAnchor(AnchorSide.Top, AnchorAlignment.End)
    data object BottomStart : AppDropdownPanelAnchor(AnchorSide.Bottom, AnchorAlignment.Start)
    data object BottomEnd : AppDropdownPanelAnchor(AnchorSide.Bottom, AnchorAlignment.End)
}
