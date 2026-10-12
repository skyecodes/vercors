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
private const val VIEWPORT_CLASS = "demo-viewport"
private const val READY_ATTRIBUTE = "data-demo-ready"

// The size the screenshots are captured at (at 1.5x); the demo always lays out at it and is scaled like an image
const val DEMO_WINDOW_WIDTH_PX = 1280.0

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
    syncDemosWithSiteTheme()
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

// The demo's own state lives on the viewport it creates, never on the host: the site's Compose HTML owns the host's
// attributes and drops the ones it didn't set whenever it updates them
@OptIn(ExperimentalComposeUiApi::class)
private fun mountDemos() {
    document.querySelectorAll("[$DEMO_ATTRIBUTE]").asList()
        .filterIsInstance<HTMLElement>()
        .filter { it.querySelector(":scope > .$VIEWPORT_CLASS") == null }
        .forEach { host ->
            val screen = DemoScreen.fromAttribute(host.getAttribute(DEMO_ATTRIBUTE)) ?: return@forEach
            val viewport = createViewport()
            val demo = MountedDemo(host, viewport)
            registerDemo(demo)
            val scaledWindow = createScaledWindow()
            viewport.appendChild(scaledWindow)
            host.appendChild(viewport)
            scaleWithHost(host, viewport, scaledWindow)
            ComposeViewport(scaledWindow) {
                DemoWindow(
                    screen = screen,
                    dark = demo.dark,
                    onReady = { viewport.setAttribute(READY_ATTRIBUTE, "") },
                    onThemeApplied = { revealAfterThemeSwitch(demo) },
                )
            }
        }
}

// Firefox doesn't clip the composited canvas of a transformed element to an ancestor's rounded corners, so the
// scaled window clips itself, in its own unscaled coordinates, to the radius the site gives the viewport
private fun roundScaledWindow(viewport: HTMLElement, scaledWindow: HTMLElement, scale: Double) {
    val radius = window.getComputedStyle(viewport).borderTopLeftRadius.removeSuffix("px").toDoubleOrNull() ?: return
    val scaledRadius = "${radius / scale}px"
    scaledWindow.style.borderRadius = scaledRadius
    scaledWindow.style.overflowX = "hidden"
    scaledWindow.style.overflowY = "hidden"
    scaledWindow.style.setProperty("clip-path", "inset(0 round $scaledRadius)")
}

private fun createViewport(): HTMLElement = (document.createElement("div") as HTMLElement).apply {
    className = VIEWPORT_CLASS
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
private fun scaleWithHost(host: HTMLElement, viewport: HTMLElement, scaledWindow: HTMLElement) {
    val update = {
        val bounds = host.getBoundingClientRect()
        val scale = bounds.width / DEMO_WINDOW_WIDTH_PX
        if (scale > 0) {
            scaledWindow.style.height = "${bounds.height / scale}px"
            scaledWindow.style.transform = "scale($scale)"
            roundScaledWindow(viewport, scaledWindow, scale)
        }
    }
    update()
    val resizeObserver: dynamic = js("Reflect").construct(window.asDynamic().ResizeObserver, arrayOf({ update() }))
    resizeObserver.observe(host)
}
