package app.vercors.launcher.demo

import androidx.compose.runtime.*
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.MutationObserver
import org.w3c.dom.MutationObserverInit

// Not the MutationObserverInit builder: in production builds it passes null for unset options, which observe() rejects
private val CLASS_ATTRIBUTE_OBSERVER_INIT: MutationObserverInit =
    js("({ attributes: true, attributeFilter: ['class'] })").unsafeCast<MutationObserverInit>()

// Silk keeps the site's color mode as a class on <html>; the standalone dev page has neither class
private fun isSiteDark(): Boolean {
    val classList = document.documentElement?.classList ?: return false
    return when {
        classList.contains("silk-dark") -> true
        classList.contains("silk-light") -> false
        else -> window.matchMedia("(prefers-color-scheme: dark)").matches
    }
}

@Composable
fun rememberSiteDarkMode(): State<Boolean> {
    val dark = remember { mutableStateOf(isSiteDark()) }
    DisposableEffect(Unit) {
        val observer = MutationObserver { _, _ -> dark.value = isSiteDark() }
        document.documentElement?.let {
            observer.observe(it, CLASS_ATTRIBUTE_OBSERVER_INIT)
        }
        onDispose { observer.disconnect() }
    }
    return dark
}
