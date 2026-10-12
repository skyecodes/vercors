package app.vercors.launcher.demo

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import org.w3c.dom.HTMLElement

class MountedDemo(val host: HTMLElement, val viewport: HTMLElement) {
    val dark: MutableState<Boolean> = mutableStateOf(isSiteDark())
    var themeSwitch: Int = 0
    var revealNotBefore: Double = 0.0
}
