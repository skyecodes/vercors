package app.vercors.core.ui.theme

import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.vercors.core.ui.CoreRes
import app.vercors.core.ui.Inter
import app.vercors.core.ui.InterTight
import app.vercors.feature.theme.domain.model.ThemeColor
import app.vercors.feature.theme.domain.model.ThemeColors
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
val warn = ThemeToken<Color>("warn")
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
val console = ThemeToken<TextStyle>("console")

fun AppTheme(themeColors: ThemeColors, accentColor: Long) = buildTheme {
    fun ThemeColor.toColor(): Color = when (this) {
        is ThemeColor.Argb -> Color(value)
        is ThemeColor.Named -> when (name) {
            "accent" -> Color(accentColor)
            else -> Color(0xFF000000)
        }
    }


    val fontFamily = FontFamily(
        Font(CoreRes.font.Inter, weight = FontWeight.Normal),
        Font(CoreRes.font.Inter, weight = FontWeight.Medium),
        Font(CoreRes.font.Inter, weight = FontWeight.SemiBold),
        Font(CoreRes.font.Inter, weight = FontWeight.Bold),
    )
    val fontFamilyTight = FontFamily(
        Font(CoreRes.font.InterTight, weight = FontWeight.Normal),
        Font(CoreRes.font.InterTight, weight = FontWeight.Medium),
        Font(CoreRes.font.InterTight, weight = FontWeight.SemiBold),
        Font(CoreRes.font.InterTight, weight = FontWeight.Bold),
    )

    defaultIndication = rememberRippleIndication()
    defaultContentColor = themeColors.onBackground.toColor()
    defaultTextSelectionColors = TextSelectionColors(
        handleColor = themeColors.primary.toColor(),
        backgroundColor = themeColors.primary.toColor().copy(alpha = 0.4f),
    )
    defaultTextStyle = TextStyle(
        fontFamily = fontFamily,
        fontSize = 14.sp
    )

    properties[colors] = mapOf(
        background to themeColors.background.toColor(),
        backgroundAlt to themeColors.backgroundAlt.toColor(),
        onBackground to themeColors.onBackground.toColor(),
        primary to themeColors.primary.toColor(),
        onPrimary to themeColors.onPrimary.toColor(),
        error to themeColors.error.toColor(),
        onError to themeColors.onError.toColor(),
        success to themeColors.success.toColor(),
        onSuccess to themeColors.onSuccess.toColor(),
        warn to themeColors.warn.toColor(),
        surface to themeColors.surface.toColor(),
        surface2 to themeColors.surface2.toColor(),
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
        console to TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp
        )
    )
}
