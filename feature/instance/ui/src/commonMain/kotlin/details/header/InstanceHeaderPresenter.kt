package app.vercors.feature.instance.ui.details.header

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import app.vercors.feature.instance.domain.model.InstanceError
import app.vercors.feature.instance.domain.model.InstanceIconError
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.usecase.*
import app.vercors.feature.instance.ui.InstanceRes
import app.vercors.feature.instance.ui.copy.CopyInstanceDialog
import app.vercors.feature.instance.ui.copy.CopyInstanceResult
import app.vercors.feature.instance.ui.copy.CopyInstanceTarget
import app.vercors.feature.instance.ui.details.InstanceDetailsScreen
import app.vercors.feature.instance.ui.errorOpenFolderFailed
import app.vercors.feature.instance.ui.errorRestoreIconFailed
import app.vercors.feature.instance.ui.export.ExportInstanceDialog
import app.vercors.feature.instance.ui.export.ExportInstanceTarget
import app.vercors.feature.instance.ui.list.InstanceListScreen
import app.vercors.feature.instance.ui.message
import app.vercors.feature.instance.ui.options.copy.CopyOptionsDialog
import app.vercors.feature.instance.ui.options.copy.CopyOptionsTarget
import app.vercors.feature.instance.ui.options.savedefault.SaveDefaultOptionsDialog
import app.vercors.feature.instance.ui.options.savedefault.SaveDefaultOptionsTarget
import app.vercors.feature.project.domain.model.ProjectDetailsError
import app.vercors.feature.project.domain.model.from
import app.vercors.feature.project.domain.usecase.GetProjectUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.error
import arrow.core.raise.context.either
import arrow.core.raise.recover
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@AssistedInject
class InstanceHeaderPresenter(
    @Assisted private val instanceId: InstanceId,
    @Assisted private val navigator: Navigator,
    private val globalScope: CoroutineScope,
    private val deleteInstance: DeleteInstanceUseCase,
    private val setInstanceIcon: SetInstanceIconUseCase,
    private val removeInstanceIcon: RemoveInstanceIconUseCase,
    private val getInstance: GetInstanceUseCase,
    private val getProject: GetProjectUseCase,
    private val setInstanceIconFromUrl: SetInstanceIconFromUrlUseCase,
    private val openInstanceFolder: OpenInstanceFolderUseCase,
    private val copyInstanceDialogFactory: CopyInstanceDialog.Factory,
    private val exportInstanceDialogFactory: ExportInstanceDialog.Factory,
    private val copyOptionsDialogFactory: CopyOptionsDialog.Factory,
    private val saveDefaultOptionsDialogFactory: SaveDefaultOptionsDialog.Factory,
    private val snackbarService: SnackbarService,
) : Presenter<InstanceHeaderState, InstanceHeaderEvent> {
    @Composable
    override fun present(): Store<InstanceHeaderState, InstanceHeaderEvent> {
        val (promptDeleteConfirmation, setPromptDeleteConfirmation) = remember { mutableStateOf(false) }
        val (copyInstanceDialog, setCopyInstanceDialog) = remember { mutableStateOf<CopyInstanceDialog?>(null) }
        val (exportInstanceDialog, setExportInstanceDialog) = remember { mutableStateOf<ExportInstanceDialog?>(null) }
        val (copyOptionsDialog, setCopyOptionsDialog) = remember { mutableStateOf<CopyOptionsDialog?>(null) }
        val (saveDefaultOptionsDialog, setSaveDefaultOptionsDialog) =
            remember { mutableStateOf<SaveDefaultOptionsDialog?>(null) }
        return InstanceHeaderState(
            promptDeleteConfirmation = promptDeleteConfirmation,
            copyInstanceDialog = copyInstanceDialog,
            exportInstanceDialog = exportInstanceDialog,
            copyOptionsDialog = copyOptionsDialog,
            saveDefaultOptionsDialog = saveDefaultOptionsDialog,
        ) + { event ->
            handleEvent(
                event = event,
                setPromptDeleteConfirmation = setPromptDeleteConfirmation,
                setCopyInstanceDialog = setCopyInstanceDialog,
                setExportInstanceDialog = setExportInstanceDialog,
                setCopyOptionsDialog = setCopyOptionsDialog,
                setSaveDefaultOptionsDialog = setSaveDefaultOptionsDialog,
            )
        }
    }

    private fun handleEvent(
        event: InstanceHeaderEvent,
        setPromptDeleteConfirmation: (Boolean) -> Unit,
        setCopyInstanceDialog: (CopyInstanceDialog?) -> Unit,
        setExportInstanceDialog: (ExportInstanceDialog?) -> Unit,
        setCopyOptionsDialog: (CopyOptionsDialog?) -> Unit,
        setSaveDefaultOptionsDialog: (SaveDefaultOptionsDialog?) -> Unit,
    ) {
        when (event) {
            InstanceHeaderEvent.OnDeleteClick -> setPromptDeleteConfirmation(true)
            is InstanceHeaderEvent.OnConfirmDelete -> handleConfirmDelete(event, setPromptDeleteConfirmation)
            is InstanceHeaderEvent.OnCopyClick ->
                setCopyInstanceDialog(copyInstanceDialogFactory.create(CopyInstanceTarget(instanceId, event.name)))

            is InstanceHeaderEvent.OnCopyInstanceResult -> {
                setCopyInstanceDialog(null)
                if (event.result is CopyInstanceResult.Copied) {
                    navigator.goTo(InstanceDetailsScreen(event.result.instanceId))
                }
            }

            is InstanceHeaderEvent.OnExportClick ->
                setExportInstanceDialog(
                    exportInstanceDialogFactory.create(ExportInstanceTarget(instanceId, event.name))
                )

            is InstanceHeaderEvent.OnExportInstanceResult -> setExportInstanceDialog(null)
            is InstanceHeaderEvent.OnCopyOptionsClick ->
                setCopyOptionsDialog(copyOptionsDialogFactory.create(CopyOptionsTarget(instanceId, event.gameVersion)))

            is InstanceHeaderEvent.OnCopyOptionsResult -> setCopyOptionsDialog(null)
            InstanceHeaderEvent.OnSaveDefaultOptionsClick ->
                setSaveDefaultOptionsDialog(
                    saveDefaultOptionsDialogFactory.create(SaveDefaultOptionsTarget(instanceId))
                )

            is InstanceHeaderEvent.OnSaveDefaultOptionsResult -> setSaveDefaultOptionsDialog(null)
            is InstanceHeaderEvent.OnIconSelected -> handleIconSelected(event)
            InstanceHeaderEvent.OnRemoveIconClick -> handleRemoveIconClick()
            InstanceHeaderEvent.OnRestoreIconClick -> handleRestoreIconClick()
            is InstanceHeaderEvent.OnOpenInstanceFolderClick -> handleOpenInstanceFolderClick(event)
        }
    }

    private fun handleConfirmDelete(
        event: InstanceHeaderEvent.OnConfirmDelete,
        setPromptDeleteConfirmation: (Boolean) -> Unit,
    ) {
        setPromptDeleteConfirmation(false)
        if (!event.result) return
        globalScope.launch {
            either { deleteInstance(instanceId) }
                .onLeft { error -> snackbarService.error { error.message } }
                .onRight {
                    navigator.pop()
                    navigator.goTo(InstanceListScreen)
                }
        }
    }

    private fun handleIconSelected(event: InstanceHeaderEvent.OnIconSelected) {
        globalScope.launch {
            either { setInstanceIcon(instanceId, event.path) }
                .onLeft { error -> snackbarService.error { error.message } }
        }
    }

    private fun handleRemoveIconClick() {
        globalScope.launch {
            either { removeInstanceIcon(instanceId) }.onLeft { error -> snackbarService.error { error.message } }
        }
    }

    private fun handleRestoreIconClick() {
        globalScope.launch {
            recover({
                val instance = getInstance(instanceId)
                val modpackInfo = instance.modpackInfo
                if (modpackInfo != null) {
                    recover({
                        val logoUrl = getProject(modpackInfo.projectId from modpackInfo.platformId).logoUrl
                        if (logoUrl == null) {
                            snackbarService.error { stringResource(InstanceRes.string.errorRestoreIconFailed) }
                        } else {
                            recover({ setInstanceIconFromUrl(instance, logoUrl) }) { _: InstanceIconError ->
                                snackbarService.error { stringResource(InstanceRes.string.errorRestoreIconFailed) }
                            }
                        }
                    }) { _: ProjectDetailsError ->
                        snackbarService.error { stringResource(InstanceRes.string.errorRestoreIconFailed) }
                    }
                }
            }) { _: InstanceError ->
                snackbarService.error { stringResource(InstanceRes.string.errorRestoreIconFailed) }
            }
        }
    }

    private fun handleOpenInstanceFolderClick(event: InstanceHeaderEvent.OnOpenInstanceFolderClick) {
        globalScope.launch {
            either { openInstanceFolder(instanceId, event.folder) }
                .onLeft { snackbarService.error { stringResource(InstanceRes.string.errorOpenFolderFailed) } }
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(instanceId: InstanceId, navigator: Navigator): InstanceHeaderPresenter
    }
}
