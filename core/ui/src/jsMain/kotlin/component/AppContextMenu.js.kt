package app.vercors.core.ui.component

import androidx.compose.runtime.Composable

@Composable
actual fun AppContextMenuArea(items: () -> List<AppContextMenuItem>, content: @Composable () -> Unit) {
    content()
}
