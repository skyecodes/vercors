package app.vercors.website.components.sections

import androidx.compose.runtime.Composable
import app.vercors.website.GITHUB_URL
import app.vercors.website.SITE_NAME
import app.vercors.website.toSitePalette

import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.base
import com.varabyte.kobweb.silk.style.toModifier
import org.jetbrains.compose.web.css.FlexWrap
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.px

val FooterStyle = CssStyle.base {
    val palette = colorMode.toSitePalette()
    Modifier
        .backgroundColor(palette.backgroundAlt)
        .borderTop(1.px, LineStyle.Solid, palette.surface)
        .color(palette.subtext)
        .fontSize(0.9.cssRem)
        .padding(topBottom = 1.5.cssRem, leftRight = 1.5.cssRem)
        .textAlign(TextAlign.Center)
}

@Composable
fun Footer(modifier: Modifier = Modifier) {
    Row(
        FooterStyle.toModifier().then(modifier).flexWrap(FlexWrap.Wrap).gap(0.5.cssRem),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SpanText("$SITE_NAME is free and open source software")
        SpanText("/")
        Link(GITHUB_URL, "Source code")
        SpanText("/")
        Link("$GITHUB_URL/blob/main/LICENSE", "License")
        SpanText("/")
        Link("https://kobweb.varabyte.com", "Website built with Kobweb")
    }
}
