package app.vercors.website

import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.ScrollBehavior
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.forms.ButtonStyle
import com.varabyte.kobweb.silk.components.forms.ButtonVars
import com.varabyte.kobweb.silk.components.layout.HorizontalDividerStyle
import com.varabyte.kobweb.silk.init.InitSilk
import com.varabyte.kobweb.silk.init.InitSilkContext
import com.varabyte.kobweb.silk.init.registerStyleBase
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.addVariantBase
import com.varabyte.kobweb.silk.style.base
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.modifyStyleBase
import org.jetbrains.compose.web.css.*

val BODY_FONT = arrayOf("Inter", "system-ui", "-apple-system", "Segoe UI", "Roboto", "sans-serif")
val HEADING_FONT = arrayOf("Inter Tight", *BODY_FONT)

@InitSilk
fun initSiteStyles(ctx: InitSilkContext) {
    ctx.stylesheet.registerStyle("html") {
        cssRule(CSSMediaQuery.MediaFeature("prefers-reduced-motion", StylePropertyValue("no-preference"))) {
            Modifier.scrollBehavior(ScrollBehavior.Smooth)
        }
    }

    ctx.stylesheet.registerStyleBase("body") {
        Modifier
            .fontFamily(*BODY_FONT)
            .fontSize(17.px)
            .lineHeight(1.6)
    }

    ctx.stylesheet.registerStyleBase(":is(h1, h2, h3, h4)") {
        Modifier.fontFamily(*HEADING_FONT).fontWeight(FontWeight.SemiBold).lineHeight(1.2)
    }

    ctx.theme.modifyStyleBase(HorizontalDividerStyle) {
        Modifier.fillMaxWidth()
    }
}

val HeadlineTextStyle = CssStyle {
    base {
        Modifier
            .fontFamily(*HEADING_FONT)
            .fontSize(2.25.cssRem)
            .fontWeight(FontWeight.Bold)
            .lineHeight(1.1)
    }
    Breakpoint.MD { Modifier.fontSize(3.cssRem) }
}

val SubheadlineTextStyle = CssStyle.base {
    Modifier
        .fontSize(1.2.cssRem)
        .color(colorMode.toSitePalette().subtext)
}

val SectionTitleStyle = CssStyle.base {
    Modifier
        .fontFamily(*HEADING_FONT)
        .fontSize(2.cssRem)
        .fontWeight(FontWeight.SemiBold)
        .textAlign(TextAlign.Center)
        .margin(top = 0.px, bottom = 1.5.cssRem)
}

val CardStyle = CssStyle.base {
    val palette = colorMode.toSitePalette()
    Modifier
        .backgroundColor(palette.backgroundAlt)
        .borderRadius(0.5.cssRem)
        .padding(1.25.cssRem)
}

val ScreenshotStyle = CssStyle.base {
    Modifier
        .maxWidth(100.percent)
        .borderRadius(0.5.cssRem)
        .boxShadow(offsetY = 0.5.cssRem, blurRadius = 2.cssRem, color = rgba(0, 0, 0, 0.25))
}

val PrimaryButtonVariant = ButtonStyle.addVariantBase {
    val palette = colorMode.toSitePalette()
    val primary = palette.primary.toRgb()
    Modifier
        .setVariable(ButtonVars.BackgroundDefaultColor, primary)
        .setVariable(ButtonVars.BackgroundHoverColor, primary.copyf(alpha = 0.85f))
        .setVariable(ButtonVars.BackgroundPressedColor, primary.copyf(alpha = 0.7f))
        .setVariable(ButtonVars.Color, palette.onPrimary)
        .fontWeight(FontWeight.SemiBold)
}

val CircleButtonVariant = ButtonStyle.addVariantBase {
    Modifier.padding(0.px).borderRadius(50.percent)
}

val UncoloredButtonVariant = ButtonStyle.addVariantBase {
    Modifier.setVariable(ButtonVars.BackgroundDefaultColor, Colors.Transparent)
}

val LightModeOnlyStyle = CssStyle.base {
    if (colorMode.isDark) Modifier.display(DisplayStyle.None) else Modifier
}

val DarkModeOnlyStyle = CssStyle.base {
    if (colorMode.isLight) Modifier.display(DisplayStyle.None) else Modifier
}
