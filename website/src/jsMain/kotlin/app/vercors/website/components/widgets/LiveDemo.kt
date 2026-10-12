package app.vercors.website.components.widgets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.css.Transition
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.attrsModifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.toModifier
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.ms
import org.jetbrains.compose.web.css.percent
import org.w3c.dom.HTMLScriptElement

// The demo is a multi-MB download and the launcher layout is unreadable at phone widths, which keep the screenshot
private const val DEMO_MIN_WIDTH_QUERY = "(min-width: 900px)"
private const val DEMO_SCRIPT_PATH = "/demo/vercors-demo.js"

private var demoScriptRequested = false

val LiveDemoStyle = CssStyle {
    base { Modifier.position(Position.Relative) }
    cssRule(" > .demo-viewport") {
        Modifier
            .borderRadius(0.5.cssRem)
            .overflow(Overflow.Hidden)
            .opacity(0)
            .transition(Transition.of("opacity", 300.ms))
    }
    cssRule("[data-demo-ready] > .demo-viewport") { Modifier.opacity(1) }
    // Set by the demo while the site's theme transition runs: the screenshots underneath cross-fade with the page
    cssRule("[data-demo-ready][data-demo-theme-switching] > .demo-viewport") {
        Modifier.opacity(0).transition(Transition.None)
    }
    // Compose sets a hand cursor inline on hoverable buttons, which would promise clicks the demo ignores
    cssRule(" canvas") { Modifier.styleModifier { property("cursor", "default !important") } }
}

@Composable
fun LiveDemo(name: String, description: String, modifier: Modifier = Modifier) {
    LaunchedEffect(Unit) { requestDemoScript() }
    Box(LiveDemoStyle.toModifier().then(modifier).attrsModifier { attr("data-demo", name) }) {
        Screenshot(name, description, Modifier.width(100.percent))
    }
}

private fun requestDemoScript() {
    if (demoScriptRequested || !window.matchMedia(DEMO_MIN_WIDTH_QUERY).matches) return
    demoScriptRequested = true
    val script = document.createElement("script") as HTMLScriptElement
    script.src = DEMO_SCRIPT_PATH
    script.async = true
    document.head?.appendChild(script)
}
