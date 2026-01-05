package app.vercors.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.modifier.appCard
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.surface2
import com.composeunstyled.DropdownMenu
import com.composeunstyled.DropdownMenuPanel
import com.composeunstyled.DropdownPanelAnchor
import com.composeunstyled.LocalContentColor
import com.composeunstyled.theme.Theme

@Composable
fun AppMenuButton(
    text: String? = null,
    imageVector: ImageVector? = null,
    backgroundColor: Color = Color.Unspecified,
    menuContent: @Composable AppMenuScope.() -> Unit,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
    modifier: Modifier = Modifier,
    menuModifier: Modifier = Modifier,
    anchor: AppDropdownPanelAnchor = AppDropdownPanelAnchor.BottomStart
) = with(object : AppMenuScope {
    override var expanded by remember { mutableStateOf(false) }
}) {
    BoxWithConstraints(modifier = modifier) {
        DropdownMenu(
            onExpandRequest = { expanded = true }
        ) {
            AppIconTextButton(
                onClick = { expanded = !expanded },
                backgroundColor = backgroundColor,
                text = text,
                imageVector = imageVector,
                contentPadding = contentPadding,
            )
        }

        DropdownMenuPanel(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = menuModifier.appCard(backgroundColor = Theme[colors][surface2])
                .widthIn(min = minWidth, max = maxWidth),
            anchor = anchor.convert()
        ) {
            menuContent()
        }
    }
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

sealed interface AppDropdownPanelAnchor {
    data object TopStart : AppDropdownPanelAnchor
    data object TopEnd : AppDropdownPanelAnchor
    data object BottomStart : AppDropdownPanelAnchor
    data object BottomEnd : AppDropdownPanelAnchor
    data object CenterStart : AppDropdownPanelAnchor
    data object CenterEnd : AppDropdownPanelAnchor
}

private fun AppDropdownPanelAnchor.convert(): DropdownPanelAnchor = when (this) {
    AppDropdownPanelAnchor.BottomEnd -> DropdownPanelAnchor.BottomEnd
    AppDropdownPanelAnchor.BottomStart -> DropdownPanelAnchor.BottomStart
    AppDropdownPanelAnchor.CenterEnd -> DropdownPanelAnchor.CenterEnd
    AppDropdownPanelAnchor.CenterStart -> DropdownPanelAnchor.CenterStart
    AppDropdownPanelAnchor.TopEnd -> DropdownPanelAnchor.TopEnd
    AppDropdownPanelAnchor.TopStart -> DropdownPanelAnchor.TopStart
}
