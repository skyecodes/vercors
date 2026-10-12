package app.vercors.launcher.demo

import kotlinx.browser.document
import kotlinx.browser.window

// Silk keeps the site's color mode as a class on <html>; the standalone dev page has neither class
fun isSiteDark(): Boolean {
    val classList = document.documentElement?.classList ?: return false
    return when {
        classList.contains("silk-dark") -> true
        classList.contains("silk-light") -> false
        else -> window.matchMedia("(prefers-color-scheme: dark)").matches
    }
}
