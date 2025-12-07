package app.vercors.launcher.gui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.ContentAlpha.disabled
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import app.vercors.launcher.gui.res.*
import app.vercors.shared.ui.colors
import app.vercors.shared.ui.component.AppButton
import app.vercors.shared.ui.component.AppIcon
import app.vercors.shared.ui.component.AppText
import app.vercors.shared.ui.error
import app.vercors.shared.ui.onError
import com.composables.icons.lucide.*
import com.composeunstyled.LocalContentColor
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.stringResource
import pro.respawn.flowmvi.api.IntentReceiver

@Composable
context(windowScope: WindowScope, store: IntentReceiver<AppIntent>)
fun AppTopBar(
    canGoBack: Boolean,
    canGoNext: Boolean,
    isMinimized: Boolean,
    onWindowEvent: (AppWindowEvent) -> Unit
) {
    windowScope.WindowDraggableArea {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)
        ) {
            Row(
                modifier = Modifier.weight(1f).padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIcon(
                    imageVector = Lucide.MountainSnow,
                    contentDescription = stringResource(Res.string.appTitle)
                )
                AppText(stringResource(Res.string.appTitle))
                AppNavButton(
                    imageVector = Lucide.ChevronLeft,
                    contentDescription = stringResource(Res.string.back),
                    enabled = canGoBack,
                    onClick = { store.intent(AppIntent.Back) }
                )
                AppNavButton(
                    imageVector = Lucide.ChevronRight,
                    contentDescription = stringResource(Res.string.next),
                    enabled = canGoNext,
                    onClick = { store.intent(AppIntent.Next) }
                )
                Spacer(Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxHeight()
            ) {
                AppWindowButton(
                    onClick = { onWindowEvent(AppWindowEvent.Minimize) },
                    imageVector = Lucide.Minus,
                    contentDescription = stringResource(Res.string.minimize)
                )
                AppWindowButton(
                    onClick = { onWindowEvent(AppWindowEvent.Maximize) },
                    imageVector = if (isMinimized) Lucide.Maximize2 else Lucide.Minimize2,
                    contentDescription = stringResource(Res.string.maximize)
                )

                val interactionSource = remember { MutableInteractionSource() }
                val hovered by interactionSource.collectIsHoveredAsState()
                AppWindowButton(
                    onClick = { onWindowEvent(AppWindowEvent.Close) },
                    imageVector = Lucide.X,
                    contentDescription = stringResource(Res.string.close),
                    tint = if (hovered) Theme[colors][onError] else LocalContentColor.current,
                    modifier = Modifier.hoverable(interactionSource) then if (hovered) Modifier.background(
                        Theme[colors][error]
                    ) else Modifier.Companion,
                )
            }
        }
    }
}

@Composable
private fun AppNavButton(
    imageVector: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    AppIcon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = Modifier.size(20.dp).clickable(
            interactionSource = interactionSource,
            enabled = enabled,
            onClick = onClick,
            indication = null
        ) then (if (enabled) Modifier.pointerHoverIcon(PointerIcon.Hand) else Modifier),
        tint = if (enabled) LocalContentColor.current else LocalContentColor.current.copy(alpha = disabled)
    )
}

@Composable
private fun AppWindowButton(
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String,
    tint: Color = LocalContentColor.current,
    modifier: Modifier = Modifier
) {
    AppButton(
        modifier = Modifier.fillMaxHeight() then modifier,
        onClick = onClick,
        shape = RectangleShape,
        contentPadding = PaddingValues.Zero
    ) {
        AppIcon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = Modifier.padding(horizontal = 8.dp),
            tint = tint
        )
    }
}
