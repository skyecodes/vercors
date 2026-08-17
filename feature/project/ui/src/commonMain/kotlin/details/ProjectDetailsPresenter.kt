package app.vercors.feature.project.ui.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import app.vercors.core.ui.state.produceProgressiveState
import app.vercors.feature.project.domain.usecase.GetProjectUseCase
import app.vercors.shared.domain.usecase.OpenBrowserUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.Store
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.launch
import opensavvy.state.ExperimentalProgressiveRaiseApi
import opensavvy.state.arrow.progressive

@AssistedInject
class ProjectDetailsPresenter(
    @Assisted private val screen: ProjectDetailsScreen,
    private val getProject: GetProjectUseCase,
    private val openBrowser: OpenBrowserUseCase,
) : Presenter<ProjectDetailsState, ProjectDetailsEvent> {
    @OptIn(ExperimentalProgressiveRaiseApi::class)
    @Composable
    override fun present(): Store<ProjectDetailsState, ProjectDetailsEvent> {
        val presenterScope = rememberCoroutineScope()
        val project by produceProgressiveState(screen.projectId) {
            value = progressive { getProject(screen.projectId).toView() }
        }

        return ProjectDetailsState(
            projectId = screen.projectId,
            project = project
        ) + { event ->
            when (event) {
                is ProjectDetailsEvent.OnLinkClick -> presenterScope.launch { openBrowser(event.url) }
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
