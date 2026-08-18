package app.vercors.feature.project.ui.details

import app.vercors.feature.project.ui.install.InstallModResult
import app.vercors.shared.ui.Event

sealed interface ProjectDetailsEvent : Event {
    data object OnInstallClick : ProjectDetailsEvent
    data class OnInstallResult(val result: InstallModResult) : ProjectDetailsEvent
}
