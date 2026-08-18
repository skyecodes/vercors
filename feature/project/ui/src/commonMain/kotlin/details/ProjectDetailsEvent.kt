package app.vercors.feature.project.ui.details

import app.vercors.feature.project.ui.install.InstallProjectResult
import app.vercors.shared.ui.Event

sealed interface ProjectDetailsEvent : Event {
    data object OnInstallClick : ProjectDetailsEvent
    data class OnInstallResult(val result: InstallProjectResult) : ProjectDetailsEvent
}
