package app.vercors.feature.project.ui.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import app.vercors.core.ui.state.produceProgressiveState
import app.vercors.feature.project.domain.usecase.GetProjectUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.Store
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import opensavvy.state.ExperimentalProgressiveRaiseApi
import opensavvy.state.arrow.progressive

@AssistedInject
class ProjectDetailsPresenter(
    @Assisted private val screen: ProjectDetailsScreen,
    private val getProject: GetProjectUseCase,
) : Presenter<ProjectDetailsState, ProjectDetailsEvent> {
    @OptIn(ExperimentalProgressiveRaiseApi::class)
    @Composable
    override fun present(): Store<ProjectDetailsState, ProjectDetailsEvent> {
        val project by produceProgressiveState(screen.projectId) {
            value = progressive { getProject(screen.projectId).toView() }
        }

        return ProjectDetailsState(
            projectId = screen.projectId,
            project = project
        ) + { event ->
            when (event) {
                ProjectDetailsEvent.OnInstallClick -> {
                    // TODO to implement
                }
            }
        }
    }

    @CircuitInject(ProjectDetailsScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(screen: ProjectDetailsScreen): ProjectDetailsPresenter
    }
}
