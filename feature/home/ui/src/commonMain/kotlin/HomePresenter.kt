package app.vercors.feature.home.ui

import androidx.compose.runtime.*
import app.vercors.feature.home.domain.usecase.HomeSection
import app.vercors.feature.home.domain.usecase.ObserveHomeSectionListUseCase
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.ui.InstanceActions
import app.vercors.feature.instance.ui.copy.CopyInstanceDialog
import app.vercors.feature.instance.ui.copy.CopyInstanceResult
import app.vercors.feature.instance.ui.copy.CopyInstanceTarget
import app.vercors.feature.instance.ui.create.CreateInstanceDialog
import app.vercors.feature.instance.ui.create.CreateInstanceResult
import app.vercors.feature.instance.ui.details.InstanceDetailsScreen
import app.vercors.feature.instance.ui.export.ExportInstanceDialog
import app.vercors.feature.instance.ui.export.ExportInstanceTarget
import app.vercors.feature.instance.ui.toBriefView
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.domain.model.from
import app.vercors.feature.project.ui.details.ProjectDetailsScreen
import app.vercors.feature.project.ui.install.*
import app.vercors.feature.project.ui.list.ProjectListScreen
import app.vercors.feature.project.ui.toView
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.error
import arrow.core.raise.context.either
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceAndCollectAsRetainedState
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import opensavvy.state.progressive.map

@AssistedInject
class HomePresenter(
    @Assisted private val navigator: Navigator,
    private val observeHomeSectionList: ObserveHomeSectionListUseCase,
    private val createInstanceDialog: CreateInstanceDialog,
    private val snackbarService: SnackbarService,
    private val installProjectDialogFactory: InstallProjectDialog.Factory,
    private val installModpackDialogFactory: InstallModpackDialog.Factory,
    private val copyInstanceDialogFactory: CopyInstanceDialog.Factory,
    private val exportInstanceDialogFactory: ExportInstanceDialog.Factory,
    private val instanceActions: InstanceActions,
) : Presenter<HomeState, HomeEvent> {
    @Composable
    override fun present(): Store<HomeState, HomeEvent> {
        val sections by observeHomeState()
        val (promptCreateInstance, setPromptCreateInstance) = remember { mutableStateOf(false) }
        val (installTarget, setInstallTarget) = remember { mutableStateOf<ProjectInstallTarget?>(null) }
        val (pendingDeleteInstanceId, setPendingDeleteInstanceId) = remember { mutableStateOf<InstanceId?>(null) }
        val (copyInstanceDialog, setCopyInstanceDialog) = remember { mutableStateOf<CopyInstanceDialog?>(null) }
        val (exportInstanceDialog, setExportInstanceDialog) = remember { mutableStateOf<ExportInstanceDialog?>(null) }

        return HomeState(
            sections = sections,
            promptCreateInstance = promptCreateInstance,
            createInstanceDialog = createInstanceDialog,
            installTarget = installTarget,
            pendingDeleteInstanceId = pendingDeleteInstanceId,
            copyInstanceDialog = copyInstanceDialog,
            exportInstanceDialog = exportInstanceDialog,
        ) + { event ->
            handleEvent(
                event = event,
                setPromptCreateInstance = setPromptCreateInstance,
                setInstallTarget = setInstallTarget,
                pendingDeleteInstanceId = pendingDeleteInstanceId,
                setPendingDeleteInstanceId = setPendingDeleteInstanceId,
                setCopyInstanceDialog = setCopyInstanceDialog,
                setExportInstanceDialog = setExportInstanceDialog,
            )
        }
    }

    private fun handleEvent(
        event: HomeEvent,
        setPromptCreateInstance: (Boolean) -> Unit,
        setInstallTarget: (ProjectInstallTarget?) -> Unit,
        pendingDeleteInstanceId: InstanceId?,
        setPendingDeleteInstanceId: (InstanceId?) -> Unit,
        setCopyInstanceDialog: (CopyInstanceDialog?) -> Unit,
        setExportInstanceDialog: (ExportInstanceDialog?) -> Unit,
    ) {
        when (event) {
            HomeEvent.OnCreateInstanceClick -> setPromptCreateInstance(true)
            is HomeEvent.OnCreateInstanceResult -> {
                setPromptCreateInstance(false)
                val result = event.result
                if (result is CreateInstanceResult.Created) navigator.goTo(InstanceDetailsScreen(event.result.instanceId))
                else if (result == CreateInstanceResult.SearchModpacks) navigator.goTo(ProjectListScreen(initialType = ProjectType.Modpack))
            }

            is HomeEvent.OnProjectClick -> navigator.goTo(ProjectDetailsScreen(event.projectId))
            is HomeEvent.OnInstanceClick -> navigator.goTo(InstanceDetailsScreen(event.instanceId))
            is HomeEvent.OnInstanceLaunchClick -> instanceActions.launch(event.instanceId, navigator)
            is HomeEvent.OnInstanceStopClick -> instanceActions.stop(event.instanceId)
            is HomeEvent.OnInstanceKillClick -> instanceActions.kill(event.instanceId)
            is HomeEvent.OnInstallProjectClick -> setInstallTarget(
                event.project.toInstallTarget(
                    preferredInstanceId = null,
                    installProjectDialogFactory = installProjectDialogFactory,
                    installModpackDialogFactory = installModpackDialogFactory,
                )
            )

            is HomeEvent.OnInstallResult -> {
                setInstallTarget(null)
                if (event.result is InstallProjectResult.GoToInstance) navigator.goTo(InstanceDetailsScreen(event.result.instanceId))
            }

            is HomeEvent.OnInstallModpackResult -> {
                setInstallTarget(null)
                if (event.result is InstallModpackResult.Installed) navigator.goTo(InstanceDetailsScreen(event.result.instanceId))
            }

            is HomeEvent.OnOpenInstanceFolderClick -> instanceActions.openFolder(event.instanceId)
            is HomeEvent.OnOpenProjectFromInstanceClick -> navigator.goTo(
                ProjectDetailsScreen(event.projectId from event.platformId, preferredInstanceId = event.instanceId)
            )

            is HomeEvent.OnDeleteInstanceClick -> setPendingDeleteInstanceId(event.instanceId)
            is HomeEvent.OnConfirmDeleteInstance -> handleConfirmDeleteInstance(
                event = event,
                pendingDeleteInstanceId = pendingDeleteInstanceId,
                setPendingDeleteInstanceId = setPendingDeleteInstanceId,
            )

            is HomeEvent.OnCopyInstanceClick ->
                setCopyInstanceDialog(
                    copyInstanceDialogFactory.create(
                        CopyInstanceTarget(
                            event.instanceId,
                            event.name
                        )
                    )
                )

            is HomeEvent.OnCopyInstanceResult -> {
                setCopyInstanceDialog(null)
                if (event.result is CopyInstanceResult.Copied) navigator.goTo(InstanceDetailsScreen(event.result.instanceId))
            }

            is HomeEvent.OnExportInstanceClick ->
                setExportInstanceDialog(
                    exportInstanceDialogFactory.create(
                        ExportInstanceTarget(
                            event.instanceId,
                            event.name
                        )
                    )
                )

            is HomeEvent.OnExportInstanceResult -> setExportInstanceDialog(null)
        }
    }

    private fun handleConfirmDeleteInstance(
        event: HomeEvent.OnConfirmDeleteInstance,
        pendingDeleteInstanceId: InstanceId?,
        setPendingDeleteInstanceId: (InstanceId?) -> Unit,
    ) {
        setPendingDeleteInstanceId(null)
        if (!event.result) return
        pendingDeleteInstanceId?.let(instanceActions::delete)
    }

    @Composable
    private fun observeHomeState(): State<List<HomeState.Section<out Any>>> =
        produceAndCollectAsRetainedState(initial = emptyList()) {
            either { observeHomeSectionList() }.fold(
                ifLeft = {
                    snackbarService.error { "A configuration error occurred: $it" }
                    flowOf(emptyList())
                },
                ifRight = { flow ->
                    flow.map { list ->
                        list.map { section ->
                            when (section) {
                                is HomeSection.InstanceListSection -> HomeState.Section.InstanceList(
                                    type = section.type,
                                    data = section.data.map { list -> list.map { it.toBriefView() } }
                                )

                                is HomeSection.ProjectListSection -> HomeState.Section.ProjectList(
                                    type = section.type,
                                    data = section.data.map { list -> list.map { it.toView() } }
                                )
                            }
                        }
                    }
                }
            )
        }

    @CircuitInject(HomeScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(navigator: Navigator): HomePresenter
    }
}
