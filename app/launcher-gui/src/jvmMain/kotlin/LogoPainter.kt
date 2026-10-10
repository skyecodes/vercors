package app.vercors.launcher.gui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.logo
import org.jetbrains.compose.resources.vectorResource

internal const val BRAND_ACCENT_COLOR = 0xFFCBA6F7L

@Composable
internal fun rememberLogoPainter(tint: Color): Painter {
    val painter = rememberVectorPainter(vectorResource(SharedRes.drawable.logo))
    return remember(painter, tint) { TintedPainter(painter, tint) }
}

private class TintedPainter(private val painter: Painter, private val tint: Color) : Painter() {
    override val intrinsicSize: Size get() = painter.intrinsicSize

    override fun DrawScope.onDraw() {
        with(painter) { draw(size, colorFilter = ColorFilter.tint(tint)) }
    }
}
