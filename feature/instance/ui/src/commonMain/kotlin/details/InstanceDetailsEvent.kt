package app.vercors.feature.instance.ui.details

import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.shared.ui.Event

sealed interface InstanceDetailsEvent : Event {
    data class OnTabClick(val tab: InstanceDetailsTab) : InstanceDetailsEvent
    data class OnOpenProjectClick(val platformId: String, val projectId: String) : InstanceDetailsEvent
    data class OnAddContentClick(val type: ProjectType) : InstanceDetailsEvent
}
