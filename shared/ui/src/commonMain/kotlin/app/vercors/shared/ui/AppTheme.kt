package app.vercors.shared.ui

import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
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
val success = ThemeToken<Color>("success")
val onSuccess = ThemeToken<Color>("on_success")
val surface = ThemeToken<Color>("surface")
val surface2 = ThemeToken<Color>("surface2")

val fonts = ThemeProperty<FontFamily>("fonts")
val standard = ThemeToken<FontFamily>("standard")
val tight = ThemeToken<FontFamily>("tight")

val textStyles = ThemeProperty<TextStyle>("textStyles")
val default = ThemeToken<TextStyle>("default")
val button = ThemeToken<TextStyle>("button")
val title = ThemeToken<TextStyle>("title")
val subtitle = ThemeToken<TextStyle>("subtitle")
val label = ThemeToken<TextStyle>("label")


val AppTheme = buildTheme {
    val fontFamily = FontFamily(
        Font(Res.font.Inter, weight = FontWeight.Normal),
        Font(Res.font.Inter, weight = FontWeight.Medium),
        Font(Res.font.Inter, weight = FontWeight.SemiBold),
        Font(Res.font.Inter, weight = FontWeight.Bold),
    )
    val fontFamilyTight = FontFamily(
        Font(Res.font.InterTight, weight = FontWeight.Normal),
        Font(Res.font.InterTight, weight = FontWeight.Medium),
        Font(Res.font.InterTight, weight = FontWeight.SemiBold),
        Font(Res.font.InterTight, weight = FontWeight.Bold),
    )

    defaultIndication = rememberRippleIndication()
    defaultContentColor = Color(0xffcdd6f4)
    defaultTextSelectionColors = TextSelectionColors(
        handleColor = Color(0xffcba6f7),
        backgroundColor = Color(0x7fcba6f7)
    )
    defaultTextStyle = TextStyle(
        fontFamily = fontFamily,
        fontSize = 14.sp
    )

    properties[colors] = mapOf(
        background to Color(0xff1e1e2e),
        backgroundAlt to Color(0xff181825),
        onBackground to Color(0xffcdd6f4),
        primary to Color(0xffcba6f7),
        onPrimary to Color(0xff11111b),
        error to Color(0xfff38ba8),
        onError to Color(0xff11111b),
        success to Color(0xffa6e3a1),
        onSuccess to Color(0xff11111b),
        surface to Color(0xff313244),
        surface2 to Color(0xff45475a)
    )

    properties[fonts] = mapOf(
        standard to fontFamily,
        tight to fontFamilyTight
    )

    properties[textStyles] = mapOf(
        default to defaultTextStyle,
        button to TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium
        ),
        title to TextStyle(
            fontFamily = fontFamilyTight,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp
        ),
        subtitle to TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp
        ),
        label to TextStyle(
            fontFamily = fontFamily,
            fontSize = 12.sp
        ),
    )
}
