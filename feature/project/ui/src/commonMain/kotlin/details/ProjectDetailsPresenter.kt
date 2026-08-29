package app.vercors.feature.project.ui.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import app.vercors.core.ui.state.produceProgressiveState
import app.vercors.feature.instance.ui.details.InstanceDetailsScreen
import app.vercors.feature.instance.ui.details.InstanceDetailsTab
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.domain.usecase.GetProjectUseCase
import app.vercors.feature.project.ui.install.InstallModpackDialog
import app.vercors.feature.project.ui.install.InstallModpackResult
import app.vercors.feature.project.ui.install.InstallProjectDialog
import app.vercors.feature.project.ui.install.InstallProjectResult
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
        val (promptInstall, setPromptInstall) = remember { mutableStateOf(false) }
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
            handleEvent(
                event = event,
                type = type,
                setPromptInstall = setPromptInstall,
            )
        }
    }

    private fun handleEvent(event: ProjectDetailsEvent, type: ProjectType?, setPromptInstall: (Boolean) -> Unit) {
        when (event) {
            ProjectDetailsEvent.OnInstallClick -> setPromptInstall(true)
            is ProjectDetailsEvent.OnInstallResult -> {
                setPromptInstall(false)
                if (event.result is InstallProjectResult.GoToInstance) {
                    navigator.goTo(
                        InstanceDetailsScreen(event.result.instanceId, initialTab = type?.toInstanceDetailsTab())
                    )
                }
            }

            is ProjectDetailsEvent.OnInstallModpackResult -> {
                setPromptInstall(false)
                if (event.result is InstallModpackResult.Installed) {
                    navigator.goTo(InstanceDetailsScreen(event.result.instanceId))
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

private fun ProjectType.toInstanceDetailsTab(): InstanceDetailsTab = when (this) {
    ProjectType.Mod -> InstanceDetailsTab.Mods
    ProjectType.ResourcePack -> InstanceDetailsTab.ResourcePacks
    ProjectType.ShaderPack -> InstanceDetailsTab.ShaderPacks
    ProjectType.DataPack -> InstanceDetailsTab.DataPacks
    ProjectType.World -> InstanceDetailsTab.Worlds
    ProjectType.Modpack -> InstanceDetailsTab.Options
}
