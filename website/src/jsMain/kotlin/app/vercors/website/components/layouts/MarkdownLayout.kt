package app.vercors.website.components.layouts

import androidx.compose.runtime.Composable
import app.vercors.website.toSitePalette
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.css.OverflowWrap
import com.varabyte.kobweb.compose.css.autoLength
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.data.add
import com.varabyte.kobweb.core.init.InitRoute
import com.varabyte.kobweb.core.init.InitRouteContext
import com.varabyte.kobweb.core.layout.Layout
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.toAttrs
import com.varabyte.kobwebx.markdown.markdown
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

val MarkdownStyle = CssStyle {
    base { Modifier.fillMaxWidth().maxWidth(48.cssRem).margin(leftRight = autoLength) }

    cssRule("h1") { Modifier.fontSize(2.75.cssRem).margin(top = 0.px, bottom = 2.cssRem) }
    cssRule("h2") { Modifier.fontSize(2.cssRem).margin(top = 2.5.cssRem, bottom = 1.cssRem) }
    cssRule("h3") { Modifier.fontSize(1.5.cssRem).margin(top = 2.cssRem, bottom = 0.75.cssRem) }
    cssRule("h4") { Modifier.fontSize(1.2.cssRem).margin(top = 1.5.cssRem, bottom = 0.5.cssRem) }

    cssRule("ul") { Modifier.fillMaxWidth().overflowWrap(OverflowWrap.BreakWord) }
    cssRule(" :is(li,ol,ul)") { Modifier.margin(bottom = 0.25.cssRem) }

    cssRule("img") { Modifier.maxWidth(100.percent).borderRadius(0.5.cssRem) }

    cssRule("blockquote") {
        Modifier
            .margin(leftRight = 0.px)
            .padding(leftRight = 1.cssRem)
            .borderLeft(3.px, LineStyle.Solid, colorMode.toSitePalette().primary)
            .color(colorMode.toSitePalette().subtext)
    }

    cssRule("code") {
        Modifier
            .backgroundColor(colorMode.toSitePalette().backgroundAlt)
            .padding(leftRight = 0.3.cssRem)
            .borderRadius(0.25.cssRem)
            .fontWeight(FontWeight.Medium)
    }

    cssRule("pre") { Modifier.margin(top = 0.5.cssRem, bottom = 2.cssRem).fillMaxWidth() }
    cssRule("pre > code") {
        Modifier
            .display(DisplayStyle.Block)
            .fillMaxWidth()
            .border(1.px, LineStyle.Solid, colorMode.toSitePalette().surface)
            .borderRadius(0.5.cssRem)
            .padding(1.cssRem)
            .fontSize(0.9.cssRem)
            .overflow { x(Overflow.Auto) }
    }
}

@InitRoute
fun initMarkdownLayout(ctx: InitRouteContext) {
    val title = ctx.markdown!!.frontMatter["title"]?.singleOrNull()
    require(title != null) { "Markdown file must set \"title\" in frontmatter" }

    ctx.data.add(PageLayoutData(title))
}

@Composable
@Layout(".components.layouts.PageLayout")
fun MarkdownLayout(content: @Composable () -> Unit) {
    Div(MarkdownStyle.toAttrs()) {
        content()
    }
}
