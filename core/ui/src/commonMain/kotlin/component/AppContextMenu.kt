package app.vercors.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

class AppContextMenuItem(
    val label: String,
    val imageVector: ImageVector? = null,
    val contentColor: Color? = null,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

@Composable
expect fun AppContextMenuArea(items: () -> List<AppContextMenuItem>, content: @Composable () -> Unit)
