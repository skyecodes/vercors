package app.vercors.core.ui.theme

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
val surface3 = ThemeToken<Color>("surface3")

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
    val convertedThemeColors = remember(themeColors, accentColor) {
        themeColors.toColor(accentColor)
    }

    val animatedColors = updateTransition(
        targetState = convertedThemeColors,
        label = "themeTransition"
    ).animateThemeColors()

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
    defaultContentColor = animatedColors.onBackgroundColor
    defaultTextSelectionColors = TextSelectionColors(
        handleColor = animatedColors.primaryColor,
        backgroundColor = animatedColors.primaryColor.copy(alpha = 0.4f),
    )
    defaultTextStyle = TextStyle(
        fontFamily = fontFamily,
        fontSize = 14.sp
    )

    properties[colors] = animatedColors.toThemeMap()

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

private fun ThemeColors.toColor(accentColor: Long) = ConvertedThemeColors(
    backgroundColor = background.toColor(accentColor),
    backgroundAltColor = backgroundAlt.toColor(accentColor),
    onBackgroundColor = onBackground.toColor(accentColor),
    primaryColor = primary.toColor(accentColor),
    onPrimaryColor = onPrimary.toColor(accentColor),
    errorColor = error.toColor(accentColor),
    onErrorColor = onError.toColor(accentColor),
    successColor = success.toColor(accentColor),
    onSuccessColor = onSuccess.toColor(accentColor),
    warnColor = warn.toColor(accentColor),
    surfaceColor = surface.toColor(accentColor),
    surface2Color = surface2.toColor(accentColor),
    surface3Color = surface3.toColor(accentColor),
)

private fun ThemeColor.toColor(accentColor: Long): Color = when (this) {
    is ThemeColor.Argb -> Color(value)
    is ThemeColor.Named -> when (name) {
        "accent" -> Color(accentColor)
        else -> Color(0xFF000000)
    }
}

@Composable
private fun Transition<ConvertedThemeColors>.animateThemeColors(): ConvertedThemeColors {
    @Composable
    fun animate(
        label: String,
        targetValue: @Composable (ConvertedThemeColors) -> Color,
    ): Color {
        val color by animateColor(
            label = label,
            transitionSpec = { tween(durationMillis = 500, easing = LinearOutSlowInEasing) },
            targetValueByState = targetValue,
        )
        return color
    }

    return ConvertedThemeColors(
        backgroundColor = animate("background") { it.backgroundColor },
        backgroundAltColor = animate("backgroundAlt") { it.backgroundAltColor },
        onBackgroundColor = animate("onBackground") { it.onBackgroundColor },
        primaryColor = animate("primary") { it.primaryColor },
        onPrimaryColor = animate("onPrimary") { it.onPrimaryColor },
        errorColor = animate("error") { it.errorColor },
        onErrorColor = animate("onError") { it.onErrorColor },
        successColor = animate("success") { it.successColor },
        onSuccessColor = animate("onSuccess") { it.onSuccessColor },
        warnColor = animate("warn") { it.warnColor },
        surfaceColor = animate("surface") { it.surfaceColor },
        surface2Color = animate("surface2") { it.surface2Color },
        surface3Color = animate("surface3") { it.surface3Color }
    )
}

private fun ConvertedThemeColors.toThemeMap() = mapOf(
    background to backgroundColor,
    backgroundAlt to backgroundAltColor,
    onBackground to onBackgroundColor,
    primary to primaryColor,
    onPrimary to onPrimaryColor,
    error to errorColor,
    onError to onErrorColor,
    success to successColor,
    onSuccess to onSuccessColor,
    warn to warnColor,
    surface to surfaceColor,
    surface2 to surface2Color,
    surface3 to surface3Color,
)

private data class ConvertedThemeColors(
    val backgroundColor: Color,
    val backgroundAltColor: Color,
    val onBackgroundColor: Color,
    val primaryColor: Color,
    val onPrimaryColor: Color,
    val errorColor: Color,
    val onErrorColor: Color,
    val successColor: Color,
    val onSuccessColor: Color,
    val warnColor: Color,
    val surfaceColor: Color,
    val surface2Color: Color,
    val surface3Color: Color,
)
