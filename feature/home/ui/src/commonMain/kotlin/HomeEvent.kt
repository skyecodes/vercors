package app.vercors.feature.home.ui

import androidx.compose.runtime.Immutable
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.ui.create.CreateInstanceResult
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.shared.ui.Event

@Immutable
sealed interface HomeEvent : Event {
    data object OnCreateInstanceClick : HomeEvent
    data class OnCreateInstanceResult(val result: CreateInstanceResult) : HomeEvent
    data class OnProjectClick(val projectId: ProjectId) : HomeEvent
    data class OnInstanceClick(val instanceId: InstanceId) : HomeEvent
    data class OnInstanceLaunchClick(val instanceId: InstanceId) : HomeEvent
    data class OnInstanceStopClick(val instanceId: InstanceId) : HomeEvent
    data class OnInstanceKillClick(val instanceId: InstanceId) : HomeEvent
}
