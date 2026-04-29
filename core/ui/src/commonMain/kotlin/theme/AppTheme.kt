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

fun AppTheme(themeColors: ThemeColors) = buildTheme {
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
    defaultContentColor = Color(themeColors.onBackground)
    defaultTextSelectionColors = TextSelectionColors(
        handleColor = Color(themeColors.primary),
        backgroundColor = Color(themeColors.primary).copy(alpha = 0.4f),
    )
    defaultTextStyle = TextStyle(
        fontFamily = fontFamily,
        fontSize = 14.sp
    )

    properties[colors] = mapOf(
        background to Color(themeColors.background),
        backgroundAlt to Color(themeColors.backgroundAlt),
        onBackground to Color(themeColors.onBackground),
        primary to Color(themeColors.primary),
        onPrimary to Color(themeColors.onPrimary),
        error to Color(themeColors.error),
        onError to Color(themeColors.onError),
        success to Color(themeColors.success),
        onSuccess to Color(themeColors.onSuccess),
        warn to Color(themeColors.warn),
        surface to Color(themeColors.surface),
        surface2 to Color(themeColors.surface2),
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
