package app.vercors.shared.ui

import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.vercors.shared.ui.res.Inter
import app.vercors.shared.ui.res.InterTight
import app.vercors.shared.ui.res.Res
import com.composables.compose.ripple.rememberRippleIndication
import com.composeunstyled.theme.ThemeProperty
import com.composeunstyled.theme.ThemeToken
import com.composeunstyled.theme.buildTheme
import org.jetbrains.compose.resources.Font

val colors = ThemeProperty<Color>("colors")
val background = ThemeToken<Color>("background")
val backgroundAlt = ThemeToken<Color>("background_alt")
val onBackground = ThemeToken<Color>("on_background")
val primary = ThemeToken<Color>("primary")
val onPrimary = ThemeToken<Color>("on_primary")
val error = ThemeToken<Color>("error")
val onError = ThemeToken<Color>("on_error")

val fonts = ThemeProperty<Font>("fonts")
val standard = ThemeToken<Font>("standard")
val tight = ThemeToken<Font>("tight")

val textStyles = ThemeProperty<TextStyle>("textStyles")
val default = ThemeToken<TextStyle>("default")
val button = ThemeToken<TextStyle>("button")
val title = ThemeToken<TextStyle>("title")
val subtitle = ThemeToken<TextStyle>("subtitle")
val label = ThemeToken<TextStyle>("label")


val AppTheme = buildTheme {
    defaultIndication = rememberRippleIndication()
    defaultContentColor = Color(0xffcdd6f4)
    defaultTextSelectionColors = TextSelectionColors(
        handleColor = Color(0xffcba6f7),
        backgroundColor = Color(0x7fcba6f7)
    )
    defaultTextStyle = TextStyle(fontFamily = FontFamily(Font(Res.font.Inter)))

    properties[colors] = mapOf(
        background to Color(0xff1e1e2e),
        backgroundAlt to Color(0xff181825),
        onBackground to Color(0xffcdd6f4),
        primary to Color(0xffcba6f7),
        onPrimary to Color(0xff11111b),
        error to Color(0xfff38ba8),
        onError to Color(0xff11111b),
    )

    properties[fonts] = mapOf(
        standard to Font(Res.font.Inter),
        tight to Font(Res.font.InterTight)
    )

    properties[textStyles] = mapOf(
        default to TextStyle(fontFamily = FontFamily(Font(Res.font.Inter))),
        button to TextStyle(fontFamily = FontFamily(Font(Res.font.Inter, weight = FontWeight.Medium))),
        title to TextStyle(
            fontFamily = FontFamily(Font(Res.font.InterTight, weight = FontWeight.SemiBold)),
            fontSize = 24.sp
        ),
        subtitle to TextStyle(
            fontFamily = FontFamily(Font(Res.font.Inter, weight = FontWeight.Medium)),
            fontSize = 20.sp
        ),
        label to TextStyle(
            fontFamily = FontFamily(Font(Res.font.Inter)),
            fontSize = 12.sp
        ),
    )
}