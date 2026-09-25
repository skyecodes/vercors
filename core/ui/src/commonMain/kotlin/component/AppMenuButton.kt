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
import app.vercors.core.ui.modifier.appMenuShadow
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.surface2
import com.composables.icons.lucide.EllipsisVertical
import com.composables.icons.lucide.Lucide
import com.composeunstyled.*
import com.composeunstyled.theme.Theme

@Composable
fun AppMenuButton(
    text: String? = null,
    imageVector: ImageVector? = Lucide.EllipsisVertical,
    backgroundColor: Color = Theme[colors][surface2],
    contentPadding: PaddingValues = NormalButtonContentPadding,
    modifier: Modifier = Modifier,
    menuModifier: Modifier = Modifier.offset(y = 4.dp),
    anchor: AppDropdownPanelAnchor = AppDropdownPanelAnchor.BottomEnd,
    menuContent: @Composable () -> Unit,
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
                modifier = menuModifier.appMenuShadow()
                    .appCard(backgroundColor = backgroundColor)
                    .width(IntrinsicSize.Max),
            ) {
                menuContent()
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

@Composable
fun AppMenuItem(
    onClick: () -> Unit,
    text: String? = null,
    imageVector: ImageVector? = null,
    enabled: Boolean = true,
    contentColor: Color = LocalContentColor.current
) {
    UnstyledDropdownMenuItem(
        onClick = onClick,
        enabled = enabled,
        indication = LocalIndication.current
    ) {
        ProvideContentColor(if (enabled) contentColor else contentColor.copy(alpha = 0.8f)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (enabled) Modifier.pointerHoverIcon(PointerIcon.Hand, true) else Modifier)
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

sealed class AppDropdownPanelAnchor(
    val side: AnchorSide,
    val alignment: AnchorAlignment
) {
    data object TopStart : AppDropdownPanelAnchor(AnchorSide.Top, AnchorAlignment.Start)
    data object TopEnd : AppDropdownPanelAnchor(AnchorSide.Top, AnchorAlignment.End)
    data object BottomStart : AppDropdownPanelAnchor(AnchorSide.Bottom, AnchorAlignment.Start)
    data object BottomEnd : AppDropdownPanelAnchor(AnchorSide.Bottom, AnchorAlignment.End)
}
