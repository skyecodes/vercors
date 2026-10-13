package app.vercors.feature.instance.ui.list

import androidx.compose.runtime.*
import app.vercors.core.domain.filterIsRight
import app.vercors.core.ui.state.produceAndCollectAsProgressiveState
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.usecase.ObserveInstanceListUseCase
import app.vercors.feature.instance.ui.BriefInstanceView
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
import app.vercors.feature.project.ui.list.ProjectListScreen
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.Store
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.map
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.map

@AssistedInject
class InstanceListPresenter(
    @Assisted private val navigator: Navigator,
    private val observeInstanceList: ObserveInstanceListUseCase,
    private val createInstanceDialog: CreateInstanceDialog,
    private val copyInstanceDialogFactory: CopyInstanceDialog.Factory,
    private val exportInstanceDialogFactory: ExportInstanceDialog.Factory,
    private val instanceActions: InstanceActions,
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
        val (copyInstanceDialog, setCopyInstanceDialog) = remember { mutableStateOf<CopyInstanceDialog?>(null) }
        val (exportInstanceDialog, setExportInstanceDialog) = remember { mutableStateOf<ExportInstanceDialog?>(null) }
        return InstanceListState(
            instances = instances,
            nameFilter = nameFilter,
            versionFilter = versionFilter,
            loaderFilter = loaderFilter,
            typeFilter = typeFilter,
            promptCreateInstance = promptCreateInstance,
            createInstanceDialog = createInstanceDialog,
            pendingDeleteInstanceId = pendingDeleteInstanceId,
            copyInstanceDialog = copyInstanceDialog,
            exportInstanceDialog = exportInstanceDialog,
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
                setCopyInstanceDialog = setCopyInstanceDialog,
                setExportInstanceDialog = setExportInstanceDialog,
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
        setCopyInstanceDialog: (CopyInstanceDialog?) -> Unit,
        setExportInstanceDialog: (ExportInstanceDialog?) -> Unit,
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
            is InstanceListEvent.OnInstanceLaunchClick -> instanceActions.launch(event.instanceId, navigator)
            is InstanceListEvent.OnInstanceStopClick -> instanceActions.stop(event.instanceId)
            is InstanceListEvent.OnInstanceKillClick -> instanceActions.kill(event.instanceId)
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

            is InstanceListEvent.OnOpenInstanceFolderClick -> instanceActions.openFolder(event.instanceId)
            is InstanceListEvent.OnOpenProjectFromInstanceClick -> navigator.goTo(
                ProjectDetailsScreen(event.projectId from event.platformId, preferredInstanceId = event.instanceId)
            )

            is InstanceListEvent.OnDeleteInstanceClick -> setPendingDeleteInstanceId(event.instanceId)
            is InstanceListEvent.OnConfirmDeleteInstance -> handleConfirmDeleteInstance(
                event = event,
                pendingDeleteInstanceId = pendingDeleteInstanceId,
                setPendingDeleteInstanceId = setPendingDeleteInstanceId,
            )

            is InstanceListEvent.OnCopyInstanceClick ->
                setCopyInstanceDialog(
                    copyInstanceDialogFactory.create(
                        CopyInstanceTarget(
                            event.instanceId,
                            event.name
                        )
                    )
                )

            is InstanceListEvent.OnCopyInstanceResult -> {
                setCopyInstanceDialog(null)
                if (event.result is CopyInstanceResult.Copied) navigator.goTo(InstanceDetailsScreen(event.result.instanceId))
            }

            is InstanceListEvent.OnExportInstanceClick ->
                setExportInstanceDialog(
                    exportInstanceDialogFactory.create(
                        ExportInstanceTarget(
                            event.instanceId,
                            event.name
                        )
                    )
                )

            is InstanceListEvent.OnExportInstanceResult -> setExportInstanceDialog(null)
        }
    }

    private fun handleConfirmDeleteInstance(
        event: InstanceListEvent.OnConfirmDeleteInstance,
        pendingDeleteInstanceId: InstanceId?,
        setPendingDeleteInstanceId: (InstanceId?) -> Unit,
    ) {
        setPendingDeleteInstanceId(null)
        if (!event.result) return
        pendingDeleteInstanceId?.let(instanceActions::delete)
    }

    @Composable
    private fun observeInstanceListView(): State<ProgressiveOutcome<Nothing, List<BriefInstanceView>>> =
        produceAndCollectAsProgressiveState {
            observeInstanceList().map { res ->
                res.map { list ->
                    list.filterIsRight().sortedByDescending { it.instance.lastPlayedAt }.map { it.toBriefView() }
                }
            }
        }

    @CircuitInject(InstanceListScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(navigator: Navigator): InstanceListPresenter
    }
}
