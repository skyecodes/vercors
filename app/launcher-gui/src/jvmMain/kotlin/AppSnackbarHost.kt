package app.vercors.launcher.gui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.animation.AppAnimatedVisibility
import app.vercors.core.ui.component.AppIcon
import app.vercors.core.ui.component.AppText
import app.vercors.core.ui.modifier.appCard
import app.vercors.core.ui.theme.*
import app.vercors.shared.ui.SnackbarMessage
import com.composables.icons.lucide.*
import com.composeunstyled.LocalContentColor
import com.composeunstyled.ProvideContentColor
import com.composeunstyled.theme.Theme

@Composable
fun BoxScope.AppSnackbarHost(
    message: SnackbarMessage?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppAnimatedVisibility(
        visible = message != null,
        modifier = modifier.align(Alignment.BottomCenter).padding(16.dp),
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
    ) {
        if (message != null) {
            val (backgroundColor, contentColor, icon) = message.type.style()
            Row(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .appCard(backgroundColor = backgroundColor, onClick = onDismiss),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ProvideContentColor(contentColor) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        AppIcon(imageVector = icon, contentDescription = null)
                        AppText(text = message.message())
                    }
                }

            }
        }
    }
}

private data class SnackbarStyle(val backgroundColor: Color, val contentColor: Color, val icon: ImageVector)

@Composable
private fun SnackbarMessage.Type.style(): SnackbarStyle = when (this) {
    SnackbarMessage.Type.INFO -> SnackbarStyle(Theme[colors][surface2], LocalContentColor.current, Lucide.Info)
    SnackbarMessage.Type.SUCCESS -> SnackbarStyle(Theme[colors][success], Theme[colors][onSuccess], Lucide.CircleCheck)
    SnackbarMessage.Type.WARN -> SnackbarStyle(Theme[colors][warn], LocalContentColor.current, Lucide.TriangleAlert)
    SnackbarMessage.Type.ERROR -> SnackbarStyle(Theme[colors][error], Theme[colors][onError], Lucide.CircleX)
}
