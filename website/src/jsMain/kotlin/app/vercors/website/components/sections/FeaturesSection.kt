package app.vercors.website.components.sections

import androidx.compose.runtime.Composable
import app.vercors.website.CardStyle
import app.vercors.website.SectionTitleStyle
import app.vercors.website.SiteColors
import app.vercors.website.components.widgets.Screenshot
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.layout.SimpleGrid
import com.varabyte.kobweb.silk.components.layout.numColumns
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.style.toAttrs
import com.varabyte.kobweb.silk.style.toModifier
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.H3

private class Showcase(val title: String, val description: String, val screenshot: String)

private class Highlight(val title: String, val description: String, val upcoming: Boolean = false)

private val SHOWCASES = listOf(
    Showcase(
        "Fully integrated mod support",
        "Browse Modrinth and CurseForge directly from the launcher and install modpacks, mods, resource packs and shader packs.",
        "content",
    ),
    Showcase(
        "Fine-grained instance settings",
        "Override memory, Java version and launch options per instance, or inherit the global defaults. Easily enable or disable mods, or add new mods to your instance.",
        "instance",
    ),
)

private val HIGHLIGHTS = listOf(
    Highlight(
        "Modpack import",
        "Import CurseForge and Modrinth modpacks, as well as Prism Launcher exported instances."
    ),
    Highlight("Every mod loader", "Fabric, Quilt, Forge and NeoForge are supported, including legacy Forge versions."),
    Highlight("Java management", "Detects installed Java runtimes and downloads the right one when needed."),
    Highlight("Account management", "Sign in with your Microsoft account and switch between accounts."),
    Highlight("Command line", "A native CLI tool to manage and launch instances from the terminal."),
    Highlight(
        "Themes",
        "Built-in Catppuccin themes with customizable accent colors, and the ability to create custom themes."
    ),
    Highlight("Mod & modpack updates", "Keep your mods and modpacks up-to-date in a single click.", upcoming = true),
    Highlight("Settings sync", "Share game options like keybindings between your instances.", upcoming = true),
)

val ShowcaseRowStyle = CssStyle {
    base {
        Modifier
            .display(DisplayStyle.Flex)
            .flexDirection(FlexDirection.Column)
            .gap(2.cssRem)
            .alignItems(AlignItems.Center)
    }
    Breakpoint.MD { Modifier.flexDirection(FlexDirection.Row) }
}

val ShowcaseRowReversedStyle = CssStyle {
    Breakpoint.MD { Modifier.flexDirection(FlexDirection.RowReverse) }
}

@Composable
fun FeaturesSection() {
    Column(Modifier.fillMaxWidth().gap(5.cssRem), horizontalAlignment = Alignment.CenterHorizontally) {
        SHOWCASES.forEachIndexed { index, showcase ->
            FeaturesShowcaseRow(showcase, reversed = index % 2 == 1)
        }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            H2(SectionTitleStyle.toAttrs()) { SpanText("And much more") }
            SimpleGrid(numColumns(base = 1, sm = 2, lg = 4), Modifier.fillMaxWidth().gap(1.cssRem)) {
                HIGHLIGHTS.forEach { FeaturesHighlightCard(it) }
            }
        }
    }
}

@Composable
private fun FeaturesShowcaseRow(showcase: Showcase, reversed: Boolean) {
    val modifier = ShowcaseRowStyle.toModifier()
        .then(if (reversed) ShowcaseRowReversedStyle.toModifier() else Modifier)
        .fillMaxWidth()
    Div(modifier.toAttrs()) {
        Column(Modifier.flex(1).gap(0.75.cssRem)) {
            H2(Modifier.margin(0.cssRem).fontSize(1.75.cssRem).toAttrs()) { SpanText(showcase.title) }
            SpanText(showcase.description, Modifier.color(SiteColors.subtext))
        }
        Screenshot(showcase.screenshot, showcase.title, Modifier.flex(2).minWidth(0.cssRem))
    }
}

@Composable
private fun FeaturesHighlightCard(highlight: Highlight) {
    Column(CardStyle.toModifier().gap(0.5.cssRem)) {
        H3(Modifier.margin(0.cssRem).fontSize(1.1.cssRem).toAttrs()) {
            SpanText(highlight.title, Modifier.color(SiteColors.primary))
        }
        if (highlight.upcoming) {
            SpanText(
                "Coming soon!",
                Modifier.fontSize(0.75.cssRem).fontWeight(FontWeight.SemiBold).color(SiteColors.subtext)
            )
        }
        SpanText(highlight.description, Modifier.fontSize(0.95.cssRem))
    }
}
