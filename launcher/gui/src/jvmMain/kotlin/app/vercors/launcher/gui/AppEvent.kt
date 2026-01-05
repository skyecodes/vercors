package app.vercors.launcher.gui

import app.vercors.shared.ui.Event

sealed interface AppEvent : Event {
    data object ToggleDrawer : AppEvent
}
