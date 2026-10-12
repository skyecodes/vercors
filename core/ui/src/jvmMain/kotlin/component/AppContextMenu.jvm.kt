package app.vercors.core.ui.component

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.window.rememberPopupPositionProviderAtPosition
import app.vercors.core.ui.modifier.appCard
import app.vercors.core.ui.modifier.appMenuShadow
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.surface2
import com.composeunstyled.LocalContentColor
import com.composeunstyled.ProvideContentColor
import com.composeunstyled.theme.Theme

private class DesktopContextMenuItem(val item: AppContextMenuItem) :
    ContextMenuItem(item.label, item.enabled, item.onClick)

@Composable
actual fun AppContextMenuArea(items: () -> List<AppContextMenuItem>, content: @Composable () -> Unit) {
    ContextMenuArea(items = { items().map(::DesktopContextMenuItem) }, content = content)
}

object AppContextMenuRepresentation : ContextMenuRepresentation {
    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    override fun Representation(state: ContextMenuState, items: () -> List<ContextMenuItem>) {
        val status = state.status
        if (status !is ContextMenuState.Status.Open) return

        val resolvedItems = items()
        if (resolvedItems.isEmpty()) {
            SideEffect { state.status = ContextMenuState.Status.Closed }
            return
        }

        Popup(
            popupPositionProvider = rememberPopupPositionProviderAtPosition(
                positionPx = status.rect.center,
                alignment = Alignment.BottomEnd,
            ),
            onDismissRequest = { state.status = ContextMenuState.Status.Closed },
            properties = PopupProperties(focusable = true),
        ) {
            Column(
                modifier = Modifier.appMenuShadow()
                    .appCard(backgroundColor = Theme[colors][surface2])
                    .width(IntrinsicSize.Max)
                    .padding(vertical = 4.dp),
            ) {
                resolvedItems.forEach { item ->
                    AppContextMenuItemRow(
                        item = item,
                        onClick = {
                            state.status = ContextMenuState.Status.Closed
                            item.onClick()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun AppContextMenuItemRow(item: ContextMenuItem, onClick: () -> Unit) {
    val appItem = (item as? DesktopContextMenuItem)?.item
    val contentColor = appItem?.contentColor ?: LocalContentColor.current
    ProvideContentColor(if (item.enabled) contentColor else contentColor.copy(alpha = 0.8f)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = item.enabled, onClick = onClick)
                .then(if (item.enabled) Modifier.pointerHoverIcon(PointerIcon.Hand, true) else Modifier)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIconTextButtonContent(text = item.label, imageVector = appItem?.imageVector)
        }
    }
}
