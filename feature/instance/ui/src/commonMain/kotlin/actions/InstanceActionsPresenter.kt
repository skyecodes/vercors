package app.vercors.feature.instance.ui.actions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.domain.model.from
import app.vercors.feature.project.ui.details.ProjectDetailsScreen
import app.vercors.feature.project.ui.list.ProjectListScreen
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.Store
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

@AssistedInject
class InstanceActionsPresenter(
    @Assisted private val navigator: Navigator,
    private val createInstanceDialog: CreateInstanceDialog,
    private val copyInstanceDialogFactory: CopyInstanceDialog.Factory,
    private val exportInstanceDialogFactory: ExportInstanceDialog.Factory,
    private val instanceActions: InstanceActions,
) : Presenter<InstanceActionsState, InstanceActionsEvent> {
    @Composable
    override fun present(): Store<InstanceActionsState, InstanceActionsEvent> {
        val (promptCreateInstance, setPromptCreateInstance) = remember { mutableStateOf(false) }
        val (pendingDeleteInstanceId, setPendingDeleteInstanceId) = remember { mutableStateOf<InstanceId?>(null) }
        val (copyInstanceDialog, setCopyInstanceDialog) = remember { mutableStateOf<CopyInstanceDialog?>(null) }
        val (exportInstanceDialog, setExportInstanceDialog) = remember { mutableStateOf<ExportInstanceDialog?>(null) }
        return InstanceActionsState(
            promptCreateInstance = promptCreateInstance,
            createInstanceDialog = createInstanceDialog,
            pendingDeleteInstanceId = pendingDeleteInstanceId,
            copyInstanceDialog = copyInstanceDialog,
            exportInstanceDialog = exportInstanceDialog,
        ) + { event ->
            handleEvent(
                event = event,
                setPromptCreateInstance = setPromptCreateInstance,
                pendingDeleteInstanceId = pendingDeleteInstanceId,
                setPendingDeleteInstanceId = setPendingDeleteInstanceId,
                setCopyInstanceDialog = setCopyInstanceDialog,
                setExportInstanceDialog = setExportInstanceDialog,
            )
        }
    }

    private fun handleEvent(
        event: InstanceActionsEvent,
        setPromptCreateInstance: (Boolean) -> Unit,
        pendingDeleteInstanceId: InstanceId?,
        setPendingDeleteInstanceId: (InstanceId?) -> Unit,
        setCopyInstanceDialog: (CopyInstanceDialog?) -> Unit,
        setExportInstanceDialog: (ExportInstanceDialog?) -> Unit,
    ) {
        when (event) {
            InstanceActionsEvent.OnCreateInstanceClick -> setPromptCreateInstance(true)
            is InstanceActionsEvent.OnCreateInstanceResult -> handleCreateInstanceResult(
                event = event,
                setPromptCreateInstance = setPromptCreateInstance,
            )

            is InstanceActionsEvent.OnInstanceClick -> navigator.goTo(InstanceDetailsScreen(event.instanceId))
            is InstanceActionsEvent.OnInstanceLaunchClick -> instanceActions.launch(event.instanceId, navigator)
            is InstanceActionsEvent.OnInstanceStopClick -> instanceActions.stop(event.instanceId)
            is InstanceActionsEvent.OnInstanceKillClick -> instanceActions.kill(event.instanceId)
            is InstanceActionsEvent.OnOpenInstanceFolderClick -> instanceActions.openFolder(event.instanceId)
            is InstanceActionsEvent.OnOpenProjectFromInstanceClick -> navigator.goTo(
                ProjectDetailsScreen(event.projectId from event.platformId, preferredInstanceId = event.instanceId)
            )

            is InstanceActionsEvent.OnDeleteInstanceClick -> setPendingDeleteInstanceId(event.instanceId)
            is InstanceActionsEvent.OnConfirmDeleteInstance -> handleConfirmDeleteInstance(
                event = event,
                pendingDeleteInstanceId = pendingDeleteInstanceId,
                setPendingDeleteInstanceId = setPendingDeleteInstanceId,
            )

            is InstanceActionsEvent.OnCopyInstanceClick ->
                setCopyInstanceDialog(
                    copyInstanceDialogFactory.create(
                        CopyInstanceTarget(
                            event.instanceId,
                            event.name
                        )
                    )
                )

            is InstanceActionsEvent.OnCopyInstanceResult -> {
                setCopyInstanceDialog(null)
                if (event.result is CopyInstanceResult.Copied) navigator.goTo(InstanceDetailsScreen(event.result.instanceId))
            }

            is InstanceActionsEvent.OnExportInstanceClick ->
                setExportInstanceDialog(
                    exportInstanceDialogFactory.create(ExportInstanceTarget(event.instanceId, event.name))
                )

            is InstanceActionsEvent.OnExportInstanceResult -> setExportInstanceDialog(null)
        }
    }

    private fun handleCreateInstanceResult(
        event: InstanceActionsEvent.OnCreateInstanceResult,
        setPromptCreateInstance: (Boolean) -> Unit,
    ) {
        setPromptCreateInstance(false)
        when (val result = event.result) {
            is CreateInstanceResult.Created -> navigator.goTo(InstanceDetailsScreen(result.instanceId))
            is CreateInstanceResult.Updated -> navigator.goTo(InstanceDetailsScreen(result.instanceId))
            CreateInstanceResult.SearchModpacks -> navigator.goTo(ProjectListScreen(initialType = ProjectType.Modpack))
            CreateInstanceResult.Cancelled -> Unit
        }
    }

    private fun handleConfirmDeleteInstance(
        event: InstanceActionsEvent.OnConfirmDeleteInstance,
        pendingDeleteInstanceId: InstanceId?,
        setPendingDeleteInstanceId: (InstanceId?) -> Unit,
    ) {
        setPendingDeleteInstanceId(null)
        if (!event.result) return
        pendingDeleteInstanceId?.let(instanceActions::delete)
    }

    @AssistedFactory
    fun interface Factory {
        fun create(navigator: Navigator): InstanceActionsPresenter
    }
}
