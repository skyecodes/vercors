package app.vercors.feature.project.ui.details

import app.vercors.shared.ui.Event

sealed interface ProjectDetailsEvent : Event {
    data object OnInstallClick : ProjectDetailsEvent
}
