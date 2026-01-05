package app.vercors.feature.project.ui.details

import androidx.compose.runtime.Composable
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.plus
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

@AssistedInject
class ProjectDetailsPresenter(
    @Assisted private val screen: ProjectDetailsScreen,
) : Presenter<ProjectDetailsState, ProjectDetailsEvent> {
    @Composable
    override fun present(): Store<ProjectDetailsState, ProjectDetailsEvent> {
        return ProjectDetailsState(
            projectId = screen.projectId
        ) + {}
    }

    @CircuitInject(ProjectDetailsScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(screen: ProjectDetailsScreen): ProjectDetailsPresenter
    }
}
