package app.vercors.launcher.demo

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.configureWebResources
import org.w3c.dom.*

private const val SCRIPT_NAME = "vercors-demo.js"
private const val DEMO_ATTRIBUTE = "data-demo"
private const val MOUNTED_ATTRIBUTE = "data-demo-mounted"
private const val READY_ATTRIBUTE = "data-demo-ready"
private const val THEME_SWITCHING_ATTRIBUTE = "data-demo-theme-switching"

// The size the screenshots are captured at (at 1.5x); the demo always lays out at it and is scaled like an image
const val DEMO_WINDOW_WIDTH_PX = 1280.0
private const val THEME_SWITCH_DURATION_MS = 1000

private var themeSwitchTimeout: Int? = null

private val BLOCKED_EVENTS = listOf(
    "pointerdown", "pointerup", "mousedown", "mouseup", "click", "dblclick", "auxclick", "contextmenu",
    "wheel", "keydown", "keyup", "touchstart", "touchend",
)

@OptIn(ExperimentalResourceApi::class)
fun main() {
    initCoroutineDispatchersIsolated()
    val baseUrl = scriptBaseUrl()
    configureWebResources {
        resourcePathMapping { path -> baseUrl + path }
    }
    blockInteractions()
    hideDemosDuringThemeSwitches()
    mountDemos()
    // Kobweb navigates client-side, so hosts can appear long after this script ran
    document.body?.let {
        MutationObserver { _, _ -> mountDemos() }
            .observe(it, js("({ childList: true, subtree: true })").unsafeCast<MutationObserverInit>())
    }
}

// kotlinx-coroutines creates its window dispatcher once and caches it on window.coroutineDispatcher, where it would pick up
// the one the site's own Kotlin/JS runtime created (a foreign class: ClassCastException); create ours against an empty
// slot, then hand the site its own back
private fun initCoroutineDispatchersIsolated() {
    val global = window.asDynamic()
    val siteDispatcher = global.coroutineDispatcher
    global.coroutineDispatcher = undefined
    Dispatchers.Default
    global.coroutineDispatcher = siteDispatcher
}

// main() runs after skiko loads asynchronously, when document.currentScript is already null
private fun scriptBaseUrl(): String {
    val script = document.querySelector("script[src\$='$SCRIPT_NAME']") as? HTMLScriptElement
    return script?.src?.substringBeforeLast('/')?.plus("/") ?: (window.location.href.substringBeforeLast('/') + "/")
}

// Registered on window before Compose mounts so these capture listeners run before any of Compose's own; hover still
// reaches the canvas, and the wheel scrolls the page because nothing calls preventDefault on it
private fun blockInteractions() {
    BLOCKED_EVENTS.forEach { type ->
        window.addEventListener(type, { event ->
            val target = event.target as? Element ?: return@addEventListener
            if (target.closest("[$DEMO_ATTRIBUTE]") == null) return@addEventListener
            event.stopImmediatePropagation()
            if (type == "contextmenu") event.preventDefault()
        }, true)
    }
}

// The demo's colors animate on their own clock, behind the site's CSS transitions; the screenshots underneath
// cross-fade in step with the page instead, then the demo comes back already in the new theme
private fun hideDemosDuringThemeSwitches() {
    val root = document.documentElement ?: return
    var dark = root.classList.contains("silk-dark")
    MutationObserver { _, _ ->
        val nowDark = root.classList.contains("silk-dark")
        if (nowDark == dark) return@MutationObserver
        dark = nowDark
        demoHosts().forEach { it.setAttribute(THEME_SWITCHING_ATTRIBUTE, "") }
        themeSwitchTimeout?.let(window::clearTimeout)
        themeSwitchTimeout = window.setTimeout({
            demoHosts().forEach { it.removeAttribute(THEME_SWITCHING_ATTRIBUTE) }
        }, THEME_SWITCH_DURATION_MS)
    }.observe(root, js("({ attributes: true, attributeFilter: ['class'] })").unsafeCast<MutationObserverInit>())
}

private fun demoHosts(): List<Element> =
    document.querySelectorAll("[$DEMO_ATTRIBUTE]").asList().filterIsInstance<Element>()

@OptIn(ExperimentalComposeUiApi::class)
private fun mountDemos() {
    document.querySelectorAll("[$DEMO_ATTRIBUTE]:not([$MOUNTED_ATTRIBUTE])").asList()
        .filterIsInstance<HTMLElement>()
        .forEach { host ->
            val screen = DemoScreen.fromAttribute(host.getAttribute(DEMO_ATTRIBUTE)) ?: return@forEach
            host.setAttribute(MOUNTED_ATTRIBUTE, "")
            val viewport = createViewport()
            val scaledWindow = createScaledWindow()
            viewport.appendChild(scaledWindow)
            host.appendChild(viewport)
            scaleWithHost(host, scaledWindow)
            ComposeViewport(scaledWindow) {
                DemoWindow(
                    screen = screen,
                    onReady = { host.setAttribute(READY_ATTRIBUTE, "") },
                )
            }
        }
}

private fun createViewport(): HTMLElement = (document.createElement("div") as HTMLElement).apply {
    className = "demo-viewport"
    style.position = "absolute"
    style.setProperty("inset", "0")
}

private fun createScaledWindow(): HTMLElement = (document.createElement("div") as HTMLElement).apply {
    style.position = "absolute"
    style.left = "0"
    style.top = "0"
    style.width = "${DEMO_WINDOW_WIDTH_PX}px"
    style.setProperty("transform-origin", "0 0")
}

// Laying out at the host's own size would make Compose round every position on a different pixel grid than the
// screenshot it replaces, visibly shifting elements at small sizes; scaling a fixed-size layout matches the image
private fun scaleWithHost(host: HTMLElement, scaledWindow: HTMLElement) {
    val update = {
        val bounds = host.getBoundingClientRect()
        val scale = bounds.width / DEMO_WINDOW_WIDTH_PX
        if (scale > 0) {
            scaledWindow.style.height = "${bounds.height / scale}px"
            scaledWindow.style.transform = "scale($scale)"
        }
    }
    update()
    val resizeObserver: dynamic = js("Reflect").construct(window.asDynamic().ResizeObserver, arrayOf({ update() }))
    resizeObserver.observe(host)
}
