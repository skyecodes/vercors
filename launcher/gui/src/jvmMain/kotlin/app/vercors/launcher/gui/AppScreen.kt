package app.vercors.launcher.gui

import app.vercors.shared.ui.Screen

data class AppScreen(
    val isMinimized: Boolean,
    val onWindowEvent: (AppWindowEvent) -> Unit
) : Screen
