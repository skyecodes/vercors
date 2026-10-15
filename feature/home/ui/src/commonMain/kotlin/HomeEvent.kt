package app.vercors.feature.home.ui

import app.vercors.feature.instance.ui.actions.InstanceActionsEvent
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.ui.BriefProjectView
import app.vercors.feature.project.ui.install.InstallModpackResult
import app.vercors.feature.project.ui.install.InstallProjectResult
import app.vercors.shared.ui.Event

sealed interface HomeEvent : Event {
    data class OnInstanceAction(val event: InstanceActionsEvent) : HomeEvent
    data class OnProjectClick(val projectId: ProjectId) : HomeEvent
    data class OnInstallProjectClick(val project: BriefProjectView) : HomeEvent
    data class OnInstallResult(val result: InstallProjectResult) : HomeEvent
    data class OnInstallModpackResult(val result: InstallModpackResult) : HomeEvent
}
