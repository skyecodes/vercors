package app.vercors.feature.instance.ui.details.content

import androidx.compose.runtime.*
import app.vercors.core.ui.state.update
import app.vercors.feature.instance.domain.model.ContentKind
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.usecase.ImportContentUseCase
import app.vercors.feature.instance.domain.usecase.ObserveInstalledContentUseCase
import app.vercors.feature.instance.domain.usecase.RemoveContentUseCase
import app.vercors.feature.instance.domain.usecase.SetContentEnabledUseCase
import app.vercors.feature.instance.ui.InstanceRes
import app.vercors.feature.instance.ui.errorImportFailed
import app.vercors.feature.instance.ui.message
import app.vercors.feature.project.domain.model.update.ContentUpdate
import app.vercors.feature.project.domain.usecase.ObserveInstanceUpdatesUseCase
import app.vercors.feature.project.domain.usecase.UpdateInstalledContentUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.error
import arrow.core.raise.context.either
import com.slack.circuit.retained.produceAndCollectAsRetainedState
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import opensavvy.state.progressive.valueOrNull
import org.jetbrains.compose.resources.stringResource

@AssistedInject
class InstalledContentPresenter(
    @Assisted private val instanceId: InstanceId,
    @Assisted private val kind: ContentKind,
    private val observeInstalledContent: ObserveInstalledContentUseCase,
    private val setContentEnabled: SetContentEnabledUseCase,
    private val removeContent: RemoveContentUseCase,
    private val importContent: ImportContentUseCase,
    private val observeInstanceUpdates: ObserveInstanceUpdatesUseCase,
    private val updateInstalledContent: UpdateInstalledContentUseCase,
    private val globalScope: CoroutineScope,
    private val snackbarService: SnackbarService,
) : Presenter<InstalledContentState, InstalledContentEvent> {
    @Composable
    override fun present(): Store<InstalledContentState, InstalledContentEvent> {
        val installed by produceAndCollectAsRetainedState(instanceId, kind, initial = null) {
            observeInstalledContent(kind, instanceId).map { outcome -> outcome.valueOrNull }
        }
        val updates by produceAndCollectAsRetainedState(instanceId, kind, initial = emptyMap()) {
            observeInstanceUpdates(instanceId).map { updates ->
                updates?.content.orEmpty().filter { it.kind == kind }.associateBy { it.fileName }
            }
        }
        val updatingFileNames = remember { mutableStateOf(emptySet<String>()) }
        val (promptRemoveConfirmation, setPromptRemoveConfirmation) =
            remember { mutableStateOf<PendingContentRemoval?>(null) }

        return InstalledContentState(
            files = installed?.map { file ->
                file.toView(updates[file.fileName]?.let {
                    ContentUpdateView(it.latest.displayName, inProgress = file.fileName in updatingFileNames.value)
                })
            },
            promptRemoveConfirmation = promptRemoveConfirmation,
        ) + { event ->
            handleEvent(
                event = event,
                promptRemoveConfirmation = promptRemoveConfirmation,
                setPromptRemoveConfirmation = setPromptRemoveConfirmation,
                updates = updates,
                updatingFileNames = updatingFileNames,
            )
        }
    }

    private fun handleEvent(
        event: InstalledContentEvent,
        promptRemoveConfirmation: PendingContentRemoval?,
        setPromptRemoveConfirmation: (PendingContentRemoval?) -> Unit,
        updates: Map<String, ContentUpdate>,
        updatingFileNames: MutableState<Set<String>>,
    ) {
        when (event) {
            is InstalledContentEvent.OnEnabledChange -> globalScope.launch {
                setContentEnabled(kind, instanceId, event.fileName, event.enabled)
            }

            is InstalledContentEvent.OnRemoveClick ->
                setPromptRemoveConfirmation(PendingContentRemoval(event.fileName, event.title))

            is InstalledContentEvent.OnConfirmRemove ->
                handleConfirmRemove(event, promptRemoveConfirmation, setPromptRemoveConfirmation)

            is InstalledContentEvent.OnImportClick -> globalScope.launch {
                either { importContent(kind, instanceId, event.sourcePath) }
                    .onLeft { snackbarService.error { stringResource(InstanceRes.string.errorImportFailed) } }
            }

            is InstalledContentEvent.OnUpdateClick -> handleUpdate(
                updates = listOfNotNull(updates[event.fileName]),
                updatingFileNames = updatingFileNames,
            )

            InstalledContentEvent.OnUpdateAllClick -> handleUpdate(
                updates = updates.values.toList(),
                updatingFileNames = updatingFileNames,
            )
        }
    }

    private fun handleUpdate(
        updates: List<ContentUpdate>,
        updatingFileNames: MutableState<Set<String>>,
    ) {
        val pending = updates.filter { it.fileName !in updatingFileNames.value }
        if (pending.isEmpty()) return
        val fileNames = pending.mapTo(HashSet()) { it.fileName }
        updatingFileNames.update { this + fileNames }
        globalScope.launch {
            either {
                updateInstalledContent(
                    instanceId = instanceId,
                    updates = pending,
                    onManualDownloadError = { error -> snackbarService.error { error.message } },
                )
            }.onLeft { error -> snackbarService.error { error.message } }
            updatingFileNames.update { this - fileNames }
        }
    }

    private fun handleConfirmRemove(
        event: InstalledContentEvent.OnConfirmRemove,
        promptRemoveConfirmation: PendingContentRemoval?,
        setPromptRemoveConfirmation: (PendingContentRemoval?) -> Unit,
    ) {
        setPromptRemoveConfirmation(null)
        if (event.result && promptRemoveConfirmation != null) {
            globalScope.launch { removeContent(kind, instanceId, promptRemoveConfirmation.fileName) }
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(instanceId: InstanceId, kind: ContentKind): InstalledContentPresenter
    }
}
