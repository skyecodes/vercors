package app.vercors.core.ui.component

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
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
) {
    var expanded by remember { mutableStateOf(false) }

    UnstyledDropdownMenu(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = it },
        side = anchor.side,
        alignment = anchor.alignment,
        panel = {
            DropdownMenuPanel(
                modifier = menuModifier.appCard(backgroundColor = Theme[colors][surface2])
                    .width(IntrinsicSize.Max),
            ) scope@{
                with(object : AppMenuScope {
                    override val innerScope = this@scope
                }) {
                    menuContent()
                }
            }
        }
    ) {
        AppIconTextButton(
            onClick = { expanded = !expanded },
            backgroundColor = backgroundColor,
            text = text,
            imageVector = imageVector,
            contentPadding = contentPadding,
        )
    }
}

interface AppMenuScope {
    val innerScope: DropdownMenuPanelScope
}

@Composable
fun AppMenuScope.AppMenuItem(
    onClick: () -> Unit,
    text: String? = null,
    imageVector: ImageVector? = null,
    enabled: Boolean = true,
    contentColor: Color = LocalContentColor.current
) {
    with(innerScope) {
        MenuItem(
            onClick = onClick,
            enabled = enabled,
            indication = LocalIndication.current
        ) {
            ProvideContentColor(if (enabled) contentColor else contentColor.copy(alpha = 0.8f)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerHoverIcon(PointerIcon.Hand, true)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIconTextButtonContent(
                        text = text,
                        imageVector = imageVector,
                    )
                }
            }
        }
    }

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
