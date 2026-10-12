package app.vercors.launcher.demo

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.MutationObserver
import org.w3c.dom.MutationObserverInit

private const val THEME_SWITCHING_ATTRIBUTE = "data-demo-theme-switching"
private const val DISABLED_ATTRIBUTE = "data-demo-disabled"

// Silk's 200ms color transition plus a margin: repainting the canvas blocks the main thread for 100+ms, so it only
// happens once the page's own transition is done, while the demo is hidden anyway
private const val SITE_COLOR_TRANSITION_MS = 300
private const val THEME_SWITCH_MIN_HIDDEN_MS = 1000.0

private val mountedDemos = mutableListOf<MountedDemo>()

// Not the MutationObserverInit builder: in production builds it passes null for unset options, which observe() rejects
private fun attributeObserverInit(name: String): MutationObserverInit {
    val init: dynamic = js("({})")
    init.attributes = true
    init.attributeFilter = arrayOf(name)
    return init.unsafeCast<MutationObserverInit>()
}

// The screenshots underneath cross-fade with the page while the demo is hidden; a demo turned off by the site's
// toggle doesn't repaint at all and catches up when it's turned back on
fun syncDemosWithSiteTheme() {
    val root = document.documentElement ?: return
    var dark = root.classList.contains("silk-dark")
    MutationObserver { _, _ ->
        val nowDark = root.classList.contains("silk-dark")
        if (nowDark == dark) return@MutationObserver
        dark = nowDark
        mountedDemos.filterNot { it.host.hasAttribute(DISABLED_ATTRIBUTE) }.forEach {
            startThemeSwitch(it, applyAfterMs = SITE_COLOR_TRANSITION_MS, minHiddenMs = THEME_SWITCH_MIN_HIDDEN_MS)
        }
    }.observe(root, attributeObserverInit("class"))
}

fun registerDemo(demo: MountedDemo) {
    mountedDemos += demo
    MutationObserver { _, _ ->
        if (!demo.host.hasAttribute(DISABLED_ATTRIBUTE) && demo.dark.value != isSiteDark()) {
            startThemeSwitch(demo, applyAfterMs = 0, minHiddenMs = 0.0)
        }
    }.observe(demo.host, attributeObserverInit(DISABLED_ATTRIBUTE))
}

private fun startThemeSwitch(demo: MountedDemo, applyAfterMs: Int, minHiddenMs: Double) {
    val switch = ++demo.themeSwitch
    demo.revealNotBefore = window.performance.now() + minHiddenMs
    demo.viewport.setAttribute(THEME_SWITCHING_ATTRIBUTE, "")
    window.setTimeout({
        if (demo.themeSwitch != switch) return@setTimeout
        val dark = isSiteDark()
        // Unchanged (toggled back): no repaint will signal, so reveal directly
        if (demo.dark.value == dark) revealAfterThemeSwitch(demo) else demo.dark.value = dark
    }, applyAfterMs)
}

fun revealAfterThemeSwitch(demo: MountedDemo) {
    val switch = demo.themeSwitch
    val remaining = demo.revealNotBefore - window.performance.now()
    window.setTimeout({
        if (demo.themeSwitch == switch) demo.viewport.removeAttribute(THEME_SWITCHING_ATTRIBUTE)
    }, remaining.toInt().coerceAtLeast(0))
}
