package app.vercors.launcher.gui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.component.AppButton
import app.vercors.core.ui.component.AppIcon
import app.vercors.core.ui.component.AppText
import app.vercors.core.ui.theme.*
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.back
import app.vercors.shared.ui.next
import com.composables.icons.lucide.*
import com.composeunstyled.LocalContentColor
import com.composeunstyled.theme.Theme
import dev.nucleusframework.application.LocalNucleusWindow
import dev.nucleusframework.window.windowDragArea
import org.jetbrains.compose.resources.stringResource

private const val DisabledContentAlpha = 0.38f

@Composable
fun AppTopBar(
    canGoBack: Boolean,
    canGoNext: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit,
) {
    val nucleusWindow = LocalNucleusWindow.current

    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max).windowDragArea()
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
            AppText(
                text = stringResource(Res.string.appTitle),
                fontFamily = Theme[fonts][tight],
                fontWeight = FontWeight.SemiBold
            )
            AppNavButton(
                imageVector = Lucide.ChevronLeft,
                contentDescription = stringResource(SharedRes.string.back),
                enabled = canGoBack,
                onClick = onNavigateBack
            )
            AppNavButton(
                imageVector = Lucide.ChevronRight,
                contentDescription = stringResource(SharedRes.string.next),
                enabled = canGoNext,
                onClick = onNavigateNext
            )
            Spacer(Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxHeight()
        ) {
            AppWindowButton(
                onClick = { nucleusWindow.setMinimized(true) },
                imageVector = Lucide.Minus,
                contentDescription = stringResource(Res.string.minimize)
            )
            AppWindowButton(
                onClick = { nucleusWindow.setMaximized(!nucleusWindow.isMaximized) },
                imageVector = if (nucleusWindow.isMaximized) Lucide.Minimize2 else Lucide.Maximize2,
                contentDescription = stringResource(Res.string.maximize)
            )

            val interactionSource = remember { MutableInteractionSource() }
            val hovered by interactionSource.collectIsHoveredAsState()
            AppWindowButton(
                onClick = { nucleusWindow.close() },
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
        tint = if (enabled) LocalContentColor.current else LocalContentColor.current.copy(alpha = DisabledContentAlpha)
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
