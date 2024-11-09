package app.vercors.launcher.core.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider

@Composable
fun rememberPopupPositionProvider(provider: CustomPopupPositionProviderContext.() -> IntOffset): PopupPositionProvider =
    remember { CustomPopupPositionProvider(provider) }

@Composable
fun rememberPopupPositionProvider(
    alignment: PopupAlignment = PopupAlignment.TopCenter,
    spacing: Int = 20
): PopupPositionProvider = rememberPopupPositionProvider {
    val x = when (alignment.horizontal) {
        PopupAlignment.Horizontal.Left -> anchorBounds.left - spacing - popupContentSize.width
        PopupAlignment.Horizontal.Center -> anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2
        PopupAlignment.Horizontal.Right -> anchorBounds.right + spacing
    }.coerceIn(0, windowSize.width - popupContentSize.width)
    val y = when (alignment.vertical) {
        PopupAlignment.Vertical.Top -> anchorBounds.top - spacing - popupContentSize.height
        PopupAlignment.Vertical.Center -> anchorBounds.top + (anchorBounds.height - popupContentSize.height) / 2
        PopupAlignment.Vertical.Bottom -> anchorBounds.bottom + spacing
    }.coerceIn(0, windowSize.height - popupContentSize.height)

    IntOffset(x, y)
}

private class CustomPopupPositionProvider(private val provider: CustomPopupPositionProviderContext.() -> IntOffset) :
    PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        return CustomPopupPositionProviderContext(
            anchorBounds,
            windowSize,
            layoutDirection,
            popupContentSize
        ).provider()
    }
}

data class CustomPopupPositionProviderContext(
    val anchorBounds: IntRect,
    val windowSize: IntSize,
    val layoutDirection: LayoutDirection,
    val popupContentSize: IntSize
)

enum class PopupAlignment(val horizontal: Horizontal, val vertical: Vertical) {
    TopLeft(Horizontal.Left, Vertical.Top),
    TopCenter(Horizontal.Center, Vertical.Top),
    TopRight(Horizontal.Right, Vertical.Top),
    CenterLeft(Horizontal.Left, Vertical.Center),
    CenterRight(Horizontal.Right, Vertical.Center),
    BottomLeft(Horizontal.Left, Vertical.Bottom),
    BottomCenter(Horizontal.Center, Vertical.Bottom),
    BottomRight(Horizontal.Right, Vertical.Bottom);

    enum class Horizontal {
        Left, Center, Right
    }

    enum class Vertical {
        Top, Center, Bottom
    }
}