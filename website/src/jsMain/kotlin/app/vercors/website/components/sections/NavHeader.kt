package app.vercors.website.components.sections

import androidx.compose.runtime.*
import app.vercors.website.*
import app.vercors.website.components.widgets.IconButton
import app.vercors.website.components.widgets.Logo
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.functions.blur
import com.varabyte.kobweb.compose.css.functions.clamp
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.icons.CloseIcon
import com.varabyte.kobweb.silk.components.icons.HamburgerIcon
import com.varabyte.kobweb.silk.components.icons.MoonIcon
import com.varabyte.kobweb.silk.components.icons.SunIcon
import com.varabyte.kobweb.silk.components.icons.fa.FaGithub
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.components.navigation.UncoloredLinkVariant
import com.varabyte.kobweb.silk.components.navigation.UndecoratedLinkVariant
import com.varabyte.kobweb.silk.components.overlay.Overlay
import com.varabyte.kobweb.silk.components.overlay.OverlayVars
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.animation.Keyframes
import com.varabyte.kobweb.silk.style.animation.toAnimation
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.style.breakpoint.displayIfAtLeast
import com.varabyte.kobweb.silk.style.breakpoint.displayUntil
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import kotlinx.browser.window
import org.jetbrains.compose.web.css.*

private val NAV_ITEMS = listOf("/" to "Home", "/about" to "About", "/blog" to "Blog")

val NavHeaderStyle = CssStyle {
    base {
        Modifier
            .fillMaxWidth()
            .position(Position.Sticky)
            .top(0.px)
            .zIndex(10)
            .padding(topBottom = 1.cssRem, leftRight = 1.5.cssRem)
            .borderBottom(1.px, LineStyle.Solid, colorMode.toSitePalette().surface)
    }
    // Blurring on a pseudo-element: backdrop-filter on the header itself would trap the side menu's fixed overlay inside it
    cssRule("::before") {
        Modifier
            .content("")
            .position(Position.Absolute)
            .top(0.px).bottom(0.px).left(0.px).right(0.px)
            .zIndex(-1)
            .backgroundColor(colorMode.toSitePalette().background.toRgb().copyf(alpha = 0.75f))
            .backdropFilter(blur(0.75.cssRem))
    }
}

val SideMenuSlideInAnim = Keyframes {
    from { Modifier.translateX(100.percent) }
    to { Modifier }
}

// The side menu keeps rendering while CLOSING so its slide-out animation can finish
enum class SideMenuState {
    CLOSED,
    OPEN,
    CLOSING;

    fun close() = when (this) {
        CLOSED -> CLOSED
        OPEN, CLOSING -> CLOSING
    }
}

@Composable
fun NavHeader() {
    Row(NavHeaderStyle.toModifier(), verticalAlignment = Alignment.CenterVertically) {
        // Equal flex on both sides keeps the menu centered regardless of the logo and button widths
        Row(Modifier.flex(1)) {
            Link("/", variant = UndecoratedLinkVariant.then(UncoloredLinkVariant)) {
                Row(Modifier.gap(0.6.cssRem), verticalAlignment = Alignment.CenterVertically) {
                    Logo(Modifier.size(2.cssRem).display(DisplayStyle.Block))
                    SpanText(
                        SITE_NAME,
                        Modifier.fontFamily(*HEADING_FONT).fontWeight(FontWeight.Bold).fontSize(1.4.cssRem)
                    )
                }
            }
        }

        Row(Modifier.gap(1.5.cssRem).displayIfAtLeast(Breakpoint.MD), verticalAlignment = Alignment.CenterVertically) {
            NavHeaderMenuItems()
        }

        Row(
            Modifier.flex(1).gap(0.25.cssRem).fontSize(1.25.cssRem).displayIfAtLeast(Breakpoint.MD),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavHeaderGitHubButton()
            NavHeaderColorModeButton()
        }

        Row(
            Modifier.fontSize(1.5.cssRem).gap(0.5.cssRem).displayUntil(Breakpoint.MD),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (menuState, setMenuState) = remember { mutableStateOf(SideMenuState.CLOSED) }

            NavHeaderGitHubButton()
            NavHeaderColorModeButton()
            IconButton(onClick = { setMenuState(SideMenuState.OPEN) }) { HamburgerIcon() }

            if (menuState != SideMenuState.CLOSED) {
                NavHeaderSideMenu(
                    menuState = menuState,
                    close = { setMenuState(menuState.close()) },
                    onAnimationEnd = { if (menuState == SideMenuState.CLOSING) setMenuState(SideMenuState.CLOSED) },
                )
            }
        }
    }
}

@Composable
private fun NavHeaderMenuItems() {
    val currentPath = rememberPageContext().route.path
    NAV_ITEMS.forEach { (path, text) ->
        val isActive = if (path == "/") currentPath == "/" else currentPath.startsWith(path)
        Link(
            path,
            text,
            Modifier
                .fontWeight(if (isActive) FontWeight.SemiBold else FontWeight.Normal)
                .color(if (isActive) SiteColors.primary else SiteColors.onBackground)
                .padding(bottom = 0.2.cssRem)
                // Transparent when inactive so switching pages doesn't shift the items
                .borderBottom(2.px, LineStyle.Solid, if (isActive) SiteColors.primary else Colors.Transparent),
            variant = UndecoratedLinkVariant.then(UncoloredLinkVariant),
        )
    }
}

@Composable
private fun NavHeaderGitHubButton() {
    IconButton(onClick = { window.open(GITHUB_URL, "_blank", "noopener") }) {
        FaGithub()
    }
}

@Composable
private fun NavHeaderColorModeButton() {
    var colorMode by ColorMode.currentState
    IconButton(onClick = { colorMode = colorMode.opposite }) {
        MoonIcon(LightModeOnlyStyle.toModifier())
        SunIcon(DarkModeOnlyStyle.toModifier())
    }
}

@Composable
private fun NavHeaderSideMenu(menuState: SideMenuState, close: () -> Unit, onAnimationEnd: () -> Unit) {
    Overlay(
        Modifier
            .setVariable(OverlayVars.BackgroundColor, Colors.Transparent)
            .onClick { close() }
    ) {
        key(menuState) {
            Column(
                Modifier
                    .fillMaxHeight()
                    .width(clamp(8.cssRem, 33.percent, 10.cssRem))
                    .align(Alignment.CenterEnd)
                    .padding(top = 1.cssRem, leftRight = 1.cssRem)
                    .gap(1.5.cssRem)
                    .backgroundColor(SiteColors.backgroundAlt)
                    .animation(
                        SideMenuSlideInAnim.toAnimation(
                            duration = 200.ms,
                            timingFunction = if (menuState == SideMenuState.OPEN) AnimationTimingFunction.EaseOut else AnimationTimingFunction.EaseIn,
                            direction = if (menuState == SideMenuState.OPEN) AnimationDirection.Normal else AnimationDirection.Reverse,
                            fillMode = AnimationFillMode.Forwards
                        )
                    )
                    .borderRadius(topLeft = 2.cssRem)
                    .onClick { it.stopPropagation() }
                    .onAnimationEnd { onAnimationEnd() },
                horizontalAlignment = Alignment.End
            ) {
                IconButton(onClick = close) { CloseIcon() }
                Column(
                    Modifier.padding(right = 0.75.cssRem).gap(1.5.cssRem).fontSize(1.4.cssRem),
                    horizontalAlignment = Alignment.End
                ) {
                    NavHeaderMenuItems()
                }
            }
        }
    }
}
