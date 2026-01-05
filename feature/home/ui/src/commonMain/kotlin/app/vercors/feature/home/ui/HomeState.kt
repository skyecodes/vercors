package app.vercors.feature.home.ui

import androidx.compose.runtime.Immutable
import app.vercors.feature.home.domain.model.HomeSectionType
import app.vercors.feature.instance.ui.BriefInstanceView
import app.vercors.feature.project.ui.BriefProjectView

@Immutable
data class HomeState(
    val sections: List<Section<*>>
) {
    @Immutable
    sealed class Section<T>(
        val type: HomeSectionType,
        val data: List<T>?
    ) {
        @Immutable
        class InstanceList(
            type: HomeSectionType,
            data: List<BriefInstanceView>?
        ) : Section<BriefInstanceView>(
            type = type,
            data = data
        )

        @Immutable
        class ProjectList(
            type: HomeSectionType,
            data: List<BriefProjectView>?
        ) : Section<BriefProjectView>(
            type = type,
            data = data
        )
    }
}
