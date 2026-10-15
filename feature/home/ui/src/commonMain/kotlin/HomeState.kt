package app.vercors.feature.home.ui

import androidx.compose.runtime.Immutable
import app.vercors.feature.home.domain.model.HomeSectionError
import app.vercors.feature.home.domain.model.HomeSectionType
import app.vercors.feature.instance.ui.BriefInstanceView
import app.vercors.feature.instance.ui.actions.InstanceActionsEvent
import app.vercors.feature.instance.ui.actions.InstanceActionsState
import app.vercors.feature.project.ui.BriefProjectView
import app.vercors.feature.project.ui.install.ProjectInstallTarget
import app.vercors.shared.ui.Store
import opensavvy.state.progressive.ProgressiveOutcome

@Immutable
data class HomeState(
    val sections: List<Section<*>>,
    val instanceActions: Store<InstanceActionsState, InstanceActionsEvent>,
    val installTarget: ProjectInstallTarget?,
) {
    @Immutable
    sealed class Section<T>(
        val type: HomeSectionType,
        val data: ProgressiveOutcome<HomeSectionError, List<T>>
    ) {
        @Immutable
        class InstanceList(
            type: HomeSectionType,
            data: ProgressiveOutcome<HomeSectionError, List<BriefInstanceView>>
        ) : Section<BriefInstanceView>(
            type = type,
            data = data
        )

        @Immutable
        class ProjectList(
            type: HomeSectionType,
            data: ProgressiveOutcome<HomeSectionError, List<BriefProjectView>>
        ) : Section<BriefProjectView>(
            type = type,
            data = data
        )
    }
}
