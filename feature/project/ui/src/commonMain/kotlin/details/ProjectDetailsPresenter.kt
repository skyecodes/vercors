package app.vercors.feature.project.ui.details

import androidx.compose.runtime.*
import app.vercors.core.ui.state.produceProgressiveState
import app.vercors.feature.instance.ui.details.InstanceDetailsScreen
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.domain.usecase.GetProjectUseCase
import app.vercors.feature.project.ui.install.InstallModpackDialog
import app.vercors.feature.project.ui.install.InstallModpackResult
import app.vercors.feature.project.ui.install.InstallProjectDialog
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.Store
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import opensavvy.state.ExperimentalProgressiveRaiseApi
import opensavvy.state.arrow.progressive
import opensavvy.state.progressive.valueOrNull

@AssistedInject
class ProjectDetailsPresenter(
    @Assisted private val screen: ProjectDetailsScreen,
    @Assisted private val navigator: Navigator,
    private val getProject: GetProjectUseCase,
    private val installProjectDialogFactory: InstallProjectDialog.Factory,
    private val installModpackDialogFactory: InstallModpackDialog.Factory,
) : Presenter<ProjectDetailsState, ProjectDetailsEvent> {
    @OptIn(ExperimentalProgressiveRaiseApi::class)
    @Composable
    override fun present(): Store<ProjectDetailsState, ProjectDetailsEvent> {
        val project by produceProgressiveState(screen.projectId) {
            value = progressive { getProject(screen.projectId).toView() }
        }
        var promptInstall by remember { mutableStateOf(false) }
        val type = project.valueOrNull?.type
        val loaders = project.valueOrNull?.loaders
        val name = project.valueOrNull?.name
        val installProjectDialog = remember(screen.projectId, type, loaders) {
            // The placeholder ProjectType.Mod is never actually installed against — the Install button (which is
            // the only way to make promptInstall true) only renders once project has loaded successfully.
            installProjectDialogFactory.create(
                screen.projectId,
                type ?: ProjectType.Mod,
                loaders ?: emptyList(),
                screen.preferredInstanceId
            )
        }
        val installModpackDialog = remember(screen.projectId, name) {
            installModpackDialogFactory.create(screen.projectId, name ?: "")
        }

        return ProjectDetailsState(
            projectId = screen.projectId,
            project = project,
            promptInstall = promptInstall,
            installProjectDialog = installProjectDialog,
            installModpackDialog = installModpackDialog
        ) + { event ->
            when (event) {
                ProjectDetailsEvent.OnInstallClick -> promptInstall = true
                is ProjectDetailsEvent.OnInstallResult -> {
                    promptInstall = false
                }

                is ProjectDetailsEvent.OnInstallModpackResult -> {
                    promptInstall = false
                    if (event.result is InstallModpackResult.Installed) {
                        navigator.goTo(InstanceDetailsScreen(event.result.instanceId))
                    }
                }
            }
        }
    }

    @CircuitInject(ProjectDetailsScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(screen: ProjectDetailsScreen, navigator: Navigator): ProjectDetailsPresenter
    }
}
