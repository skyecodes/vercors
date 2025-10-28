package com.skyecodes.vercors

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "vercors",
    ) {
        App()
    }
}