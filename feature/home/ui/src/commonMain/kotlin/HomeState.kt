package app.vercors.feature.home.ui

import androidx.compose.runtime.Immutable
import app.vercors.feature.home.domain.model.HomeSectionError
import app.vercors.feature.home.domain.model.HomeSectionType
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.ui.BriefInstanceView
import app.vercors.feature.instance.ui.copy.CopyInstanceDialog
import app.vercors.feature.instance.ui.create.CreateInstanceResult
import app.vercors.feature.instance.ui.export.ExportInstanceDialog
import app.vercors.feature.project.ui.BriefProjectView
import app.vercors.feature.project.ui.install.ProjectInstallTarget
import app.vercors.shared.ui.overlay.DialogOverlay
import opensavvy.state.progressive.ProgressiveOutcome

@Immutable
data class HomeState(
    val sections: List<Section<*>>,
    val promptCreateInstance: Boolean,
    val createInstanceDialog: DialogOverlay<CreateInstanceResult>,
    val installTarget: ProjectInstallTarget?,
    val pendingDeleteInstanceId: InstanceId?,
    val copyInstanceDialog: CopyInstanceDialog?,
    val exportInstanceDialog: ExportInstanceDialog?,
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
