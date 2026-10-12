package app.vercors.launcher.shell

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
import app.vercors.shared.ui.*
import com.composables.icons.lucide.*
import com.composeunstyled.LocalContentColor
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

private const val DisabledContentAlpha = 0.38f

@Composable
fun AppTopBar(
    canGoBack: Boolean,
    canGoNext: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit,
    appName: String,
    isMaximized: Boolean,
    onMinimize: () -> Unit,
    onToggleMaximize: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max) then modifier
    ) {
        Row(
            modifier = Modifier.weight(1f).padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(
                imageVector = vectorResource(SharedRes.drawable.logo),
                contentDescription = null,
                tint = Theme[colors][primary],
                modifier = Modifier.size(24.dp)
            )
            AppText(
                text = appName,
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
                onClick = onMinimize,
                imageVector = Lucide.Minus,
                contentDescription = stringResource(ShellRes.string.minimize)
            )
            AppWindowButton(
                onClick = onToggleMaximize,
                imageVector = if (isMaximized) Lucide.Minimize2 else Lucide.Maximize2,
                contentDescription = stringResource(ShellRes.string.maximize)
            )

            val interactionSource = remember { MutableInteractionSource() }
            val hovered by interactionSource.collectIsHoveredAsState()
            AppWindowButton(
                onClick = onClose,
                imageVector = Lucide.X,
                contentDescription = stringResource(SharedRes.string.close),
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
