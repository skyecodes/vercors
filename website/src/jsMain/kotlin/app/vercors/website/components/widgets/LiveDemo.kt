package app.vercors.website.components.widgets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.css.Transition
import com.varabyte.kobweb.compose.css.Visibility
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.attrsModifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.core.AppGlobals
import com.varabyte.kobweb.core.isExporting
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.style.toModifier
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.ms
import org.jetbrains.compose.web.css.percent
import org.w3c.dom.HTMLScriptElement

// The demo is a multi-MB download and the launcher layout is unreadable at phone widths, which keep the screenshot;
// the same breakpoint as the header's desktop row, which holds the live demo toggle
private val DEMO_MIN_BREAKPOINT = Breakpoint.MD
private const val DEMO_SCRIPT_PATH = "/demo/vercors-demo.js"
private const val LIVE_DEMOS_KEY = "website:liveDemos"

private var demoScriptRequested = false

private val liveDemosEnabledState = mutableStateOf(readLiveDemosEnabled())

var liveDemosEnabled: Boolean
    get() = liveDemosEnabledState.value
    set(value) {
        liveDemosEnabledState.value = value
        try {
            localStorage.setItem(LIVE_DEMOS_KEY, value.toString())
        } catch (e: Throwable) {
            console.error(e)
        }
    }

private fun readLiveDemosEnabled(): Boolean =
    try {
        localStorage.getItem(LIVE_DEMOS_KEY) != "false"
    } catch (e: Throwable) {
        console.error(e)
        true
    }

val LiveDemoStyle = CssStyle {
    base { Modifier.position(Position.Relative) }
    cssRule(" > .demo-viewport") {
        Modifier
            .borderRadius(0.5.cssRem)
            .overflow(Overflow.Hidden)
            // The scaled canvas is composited on its own layer, which overflow + border-radius doesn't round
            .styleModifier { property("clip-path", "inset(0 round 0.5rem)") }
            .opacity(0)
            .transition(Transition.of("opacity", 300.ms))
    }
    cssRule(" > .demo-viewport[data-demo-ready]") { Modifier.opacity(1) }
    // Hidden rather than unmounted so turning demos back on is instant; a hidden canvas gets no hover and stays idle
    cssRule("[data-demo-disabled] > .demo-viewport[data-demo-ready]") {
        Modifier
            .opacity(0)
            .visibility(Visibility.Hidden)
            .transition(Transition.of("opacity", 300.ms), Transition.of("visibility", 300.ms))
    }
    // Set by the demo while the site's theme transition runs: the screenshots underneath cross-fade with the page
    cssRule(" > .demo-viewport[data-demo-ready][data-demo-theme-switching]") {
        Modifier.opacity(0).transition(Transition.None)
    }
    // Compose sets a hand cursor inline on hoverable buttons, which would promise clicks the demo ignores
    cssRule(" canvas") { Modifier.styleModifier { property("cursor", "default !important") } }
}

@Composable
fun LiveDemo(name: String, description: String, modifier: Modifier = Modifier) {
    val enabled = liveDemosEnabled
    // The export snapshots the DOM after scripts ran: the injected script tag and the demo's mounted viewports would
    // be baked into the static page, loading the bundle twice and leaving dead viewports the demo never mounts into
    LaunchedEffect(enabled) { if (enabled && !AppGlobals.isExporting) requestDemoScript() }
    Box(
        LiveDemoStyle.toModifier().then(modifier).attrsModifier {
            attr("data-demo", name)
            if (!enabled) attr("data-demo-disabled", "")
        }
    ) {
        Screenshot(name, description, Modifier.width(100.percent))
    }
}

private fun requestDemoScript() {
    if (demoScriptRequested || !window.matchMedia(DEMO_MIN_BREAKPOINT.toCSSMediaQuery().toString()).matches) return
    demoScriptRequested = true
    val script = document.createElement("script") as HTMLScriptElement
    script.src = DEMO_SCRIPT_PATH
    script.async = true
    document.head?.appendChild(script)
}
