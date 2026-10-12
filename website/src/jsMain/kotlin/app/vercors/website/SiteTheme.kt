package app.vercors.website

import com.varabyte.kobweb.compose.css.StyleVariable
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.modifiers.setVariable
import com.varabyte.kobweb.silk.init.InitSilk
import com.varabyte.kobweb.silk.init.InitSilkContext
import com.varabyte.kobweb.silk.init.registerStyleBase
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.colors.cssClass
import com.varabyte.kobweb.silk.theme.colors.palette.*
import org.jetbrains.compose.web.css.CSSColorValue

// Mirrors the launcher's builtin catppuccin_mocha/catppuccin_latte themes with the default mauve accent
class SitePalette(
    val background: Color,
    val backgroundAlt: Color,
    val onBackground: Color,
    val subtext: Color,
    val primary: Color,
    val onPrimary: Color,
    val error: Color,
    val success: Color,
    val warn: Color,
    val surface: Color,
    val surface2: Color,
    val surface3: Color,
)

object SitePalettes {
    val mocha = SitePalette(
        background = Color.rgb(0x1E1E2E),
        backgroundAlt = Color.rgb(0x181825),
        onBackground = Color.rgb(0xCDD6F4),
        subtext = Color.rgb(0xA6ADC8),
        primary = Color.rgb(0xCBA6F7),
        onPrimary = Color.rgb(0x11111B),
        error = Color.rgb(0xF38BA8),
        success = Color.rgb(0xA6E3A1),
        warn = Color.rgb(0xFAB387),
        surface = Color.rgb(0x313244),
        surface2 = Color.rgb(0x45475A),
        surface3 = Color.rgb(0x585B70),
    )
    val latte = SitePalette(
        background = Color.rgb(0xEFF1F5),
        backgroundAlt = Color.rgb(0xE6E9EF),
        onBackground = Color.rgb(0x4C4F69),
        subtext = Color.rgb(0x6C6F85),
        primary = Color.rgb(0x8839EF),
        onPrimary = Color.rgb(0xDCE0E8),
        error = Color.rgb(0xD20F39),
        success = Color.rgb(0x40A02B),
        warn = Color.rgb(0xFE640B),
        surface = Color.rgb(0xCCD0DA),
        surface2 = Color.rgb(0xBCC0CC),
        surface3 = Color.rgb(0xACB0BE),
    )
}

private val PrimaryColorVar by StyleVariable<CSSColorValue>(prefix = "site")
private val OnBackgroundColorVar by StyleVariable<CSSColorValue>(prefix = "site")
private val SubtextColorVar by StyleVariable<CSSColorValue>(prefix = "site")
private val BackgroundAltColorVar by StyleVariable<CSSColorValue>(prefix = "site")
private val SurfaceColorVar by StyleVariable<CSSColorValue>(prefix = "site")
private val SuccessColorVar by StyleVariable<CSSColorValue>(prefix = "site")

// Inline colors must go through these instead of ColorMode.current, which the static export would bake in
object SiteColors {
    val primary: CSSColorValue = PrimaryColorVar.value()
    val onBackground: CSSColorValue = OnBackgroundColorVar.value()
    val subtext: CSSColorValue = SubtextColorVar.value()
    val backgroundAlt: CSSColorValue = BackgroundAltColorVar.value()
    val surface: CSSColorValue = SurfaceColorVar.value()
    val success: CSSColorValue = SuccessColorVar.value()
}

fun ColorMode.toSitePalette(): SitePalette = when (this) {
    ColorMode.LIGHT -> SitePalettes.latte
    ColorMode.DARK -> SitePalettes.mocha
}

@InitSilk
fun initTheme(ctx: InitSilkContext) {
    ctx.theme.palettes.light.applySitePalette(SitePalettes.latte)
    ctx.theme.palettes.dark.applySitePalette(SitePalettes.mocha)
    ColorMode.entries.forEach { colorMode ->
        ctx.stylesheet.registerStyleBase(".${colorMode.cssClass}") { colorMode.toSitePalette().toColorVariables() }
    }
}

private fun SitePalette.toColorVariables(): Modifier = Modifier
    .setVariable(PrimaryColorVar, primary)
    .setVariable(OnBackgroundColorVar, onBackground)
    .setVariable(SubtextColorVar, subtext)
    .setVariable(BackgroundAltColorVar, backgroundAlt)
    .setVariable(SurfaceColorVar, surface)
    .setVariable(SuccessColorVar, success)

private fun MutablePalette.applySitePalette(site: SitePalette) {
    background = site.background
    color = site.onBackground
    border = site.surface2
    focusOutline = site.primary
    placeholder = site.subtext
    link.default = site.primary
    link.visited = site.primary
    button.default = site.surface
    button.hover = site.surface2
    button.focus = site.surface2
    button.pressed = site.surface3
    tooltip.background = site.surface
    tooltip.color = site.onBackground
    tab.color = site.onBackground
    tab.background = site.backgroundAlt
    tab.selectedColor = site.primary
    tab.selectedBackground = site.surface
    tab.selectedBorder = site.primary
    tab.hover = site.surface
    tab.pressed = site.surface2
}
