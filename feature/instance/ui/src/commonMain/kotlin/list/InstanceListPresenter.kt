package app.vercors.feature.instance.ui.list

import androidx.compose.runtime.*
import app.vercors.core.domain.filterIsRight
import app.vercors.core.ui.state.produceAndCollectAsProgressiveState
import app.vercors.feature.instance.domain.model.InstanceFolder
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.usecase.DeleteInstanceUseCase
import app.vercors.feature.instance.domain.usecase.ObserveInstanceListUseCase
import app.vercors.feature.instance.domain.usecase.OpenInstanceFolderUseCase
import app.vercors.feature.instance.ui.*
import app.vercors.feature.instance.ui.create.CreateInstanceDialog
import app.vercors.feature.instance.ui.create.CreateInstanceResult
import app.vercors.feature.instance.ui.details.InstanceDetailsScreen
import app.vercors.feature.instance.ui.details.InstanceDetailsTab
import app.vercors.feature.launcher.domain.model.InstanceLaunchError
import app.vercors.feature.launcher.domain.usecase.KillInstanceUseCase
import app.vercors.feature.launcher.domain.usecase.LaunchInstanceUseCase
import app.vercors.feature.launcher.domain.usecase.StopInstanceUseCase
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.domain.model.from
import app.vercors.feature.project.ui.details.ProjectDetailsScreen
import app.vercors.feature.project.ui.list.ProjectListScreen
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.error
import arrow.core.raise.context.either
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.map
import org.jetbrains.compose.resources.stringResource

@AssistedInject
class InstanceListPresenter(
    @Assisted private val navigator: Navigator,
    private val observeInstanceList: ObserveInstanceListUseCase,
    private val createInstanceDialog: CreateInstanceDialog,
    private val globalScope: CoroutineScope,
    private val launchInstance: LaunchInstanceUseCase,
    private val stopInstance: StopInstanceUseCase,
    private val killInstance: KillInstanceUseCase,
    private val snackbarService: SnackbarService,
    private val openInstanceFolder: OpenInstanceFolderUseCase,
    private val deleteInstance: DeleteInstanceUseCase,
) : Presenter<InstanceListState, InstanceListEvent> {
    @Composable
    override fun present(): Store<InstanceListState, InstanceListEvent> {
        val instances by observeInstanceListView()
        val (nameFilter, setNameFilter) = remember { mutableStateOf("") }
        val (versionFilter, setVersionFilter) = remember { mutableStateOf("") }
        val (loaderFilter, setLoaderFilter) = remember { mutableStateOf("") }
        val (typeFilter, setTypeFilter) = remember { mutableStateOf("") }
        val (promptCreateInstance, setPromptCreateInstance) = remember { mutableStateOf(false) }
        val (pendingDeleteInstanceId, setPendingDeleteInstanceId) = remember { mutableStateOf<InstanceId?>(null) }
        return InstanceListState(
            instances = instances,
            nameFilter = nameFilter,
            versionFilter = versionFilter,
            loaderFilter = loaderFilter,
            typeFilter = typeFilter,
            promptCreateInstance = promptCreateInstance,
            createInstanceDialog = createInstanceDialog,
            pendingDeleteInstanceId = pendingDeleteInstanceId,
        ) + { event ->
            handleEvent(
                event = event,
                setNameFilter = setNameFilter,
                setVersionFilter = setVersionFilter,
                setLoaderFilter = setLoaderFilter,
                setTypeFilter = setTypeFilter,
                setPromptCreateInstance = setPromptCreateInstance,
                pendingDeleteInstanceId = pendingDeleteInstanceId,
                setPendingDeleteInstanceId = setPendingDeleteInstanceId,
            )
        }
    }

    private fun handleEvent(
        event: InstanceListEvent,
        setNameFilter: (String) -> Unit,
        setVersionFilter: (String) -> Unit,
        setLoaderFilter: (String) -> Unit,
        setTypeFilter: (String) -> Unit,
        setPromptCreateInstance: (Boolean) -> Unit,
        pendingDeleteInstanceId: InstanceId?,
        setPendingDeleteInstanceId: (InstanceId?) -> Unit,
    ) {
        when (event) {
            InstanceListEvent.OnCreateInstanceClick -> setPromptCreateInstance(true)
            is InstanceListEvent.OnCreateInstanceResult -> {
                setPromptCreateInstance(false)
                val result = event.result
                if (result is CreateInstanceResult.Created) navigator.goTo(InstanceDetailsScreen(result.instanceId))
                else if (result == CreateInstanceResult.SearchModpacks) navigator.goTo(ProjectListScreen(initialType = ProjectType.Modpack))
            }

            is InstanceListEvent.OnInstanceClick -> navigator.goTo(InstanceDetailsScreen(event.instanceId))
            is InstanceListEvent.OnInstanceLaunchClick -> handleLaunchClick(event.instanceId)
            is InstanceListEvent.OnInstanceStopClick -> globalScope.launch { stopInstance(event.instanceId) }
            is InstanceListEvent.OnInstanceKillClick -> globalScope.launch { killInstance(event.instanceId) }
            is InstanceListEvent.OnNameFilterChange -> setNameFilter(event.name)
            is InstanceListEvent.OnVersionFilterChange -> setVersionFilter(event.version)
            is InstanceListEvent.OnLoaderFilterChange -> setLoaderFilter(event.loader)
            is InstanceListEvent.OnTypeFilterChange -> setTypeFilter(event.type)
            InstanceListEvent.OnResetFilterClick -> {
                setNameFilter("")
                setVersionFilter("")
                setLoaderFilter("")
                setTypeFilter("")
            }

            is InstanceListEvent.OnOpenInstanceFolderClick -> handleOpenInstanceFolderClick(event.instanceId)
            is InstanceListEvent.OnOpenProjectFromInstanceClick -> navigator.goTo(
                ProjectDetailsScreen(event.projectId from event.platformId, preferredInstanceId = event.instanceId)
            )

            is InstanceListEvent.OnDeleteInstanceClick -> setPendingDeleteInstanceId(event.instanceId)
            is InstanceListEvent.OnConfirmDeleteInstance -> handleConfirmDeleteInstance(
                event = event,
                pendingDeleteInstanceId = pendingDeleteInstanceId,
                setPendingDeleteInstanceId = setPendingDeleteInstanceId,
            )
        }
    }

    private fun handleOpenInstanceFolderClick(instanceId: InstanceId) = globalScope.launch {
        val opened = openInstanceFolder(instanceId, InstanceFolder.Root)
        if (!opened) snackbarService.error { stringResource(InstanceRes.string.errorOpenFolderFailed) }
    }

    private fun handleConfirmDeleteInstance(
        event: InstanceListEvent.OnConfirmDeleteInstance,
        pendingDeleteInstanceId: InstanceId?,
        setPendingDeleteInstanceId: (InstanceId?) -> Unit,
    ) {
        setPendingDeleteInstanceId(null)
        if (!event.result) return
        val instanceId = pendingDeleteInstanceId ?: return
        globalScope.launch {
            either { deleteInstance(instanceId) }.onLeft { error -> snackbarService.error { error.message } }
        }
    }

    private fun handleLaunchClick(instanceId: InstanceId) = globalScope.launch {
        either { launchInstance(instanceId) }.onLeft { error ->
            when (error) {
                is InstanceLaunchError.NoCompatibleJava, InstanceLaunchError.LoginRequired ->
                    navigator.goTo(
                        InstanceDetailsScreen(
                            instanceId,
                            initialTab = InstanceDetailsTab.Logs,
                            autoLaunch = true
                        )
                    )

                else -> snackbarService.error { error.message }
            }
        }
    }

    @Composable
    private fun observeInstanceListView(): State<ProgressiveOutcome<Nothing, List<BriefInstanceView>>> =
        produceAndCollectAsProgressiveState {
            observeInstanceList().map { res ->
                res.map { list -> list.filterIsRight().map { it.toBriefView() } }
            }
        }

    @CircuitInject(InstanceListScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(navigator: Navigator): InstanceListPresenter
    }
}
