package app.vercors.feature.instance.ui.details.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import app.vercors.feature.instance.domain.model.ContentKind
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.usecase.ImportContentUseCase
import app.vercors.feature.instance.domain.usecase.ObserveInstalledContentUseCase
import app.vercors.feature.instance.domain.usecase.RemoveContentUseCase
import app.vercors.feature.instance.domain.usecase.SetContentEnabledUseCase
import app.vercors.feature.instance.ui.InstanceRes
import app.vercors.feature.instance.ui.errorImportFailed
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.error
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
    private val globalScope: CoroutineScope,
    private val snackbarService: SnackbarService,
) : Presenter<InstalledContentState, InstalledContentEvent> {
    @Composable
    override fun present(): Store<InstalledContentState, InstalledContentEvent> {
        val files by produceAndCollectAsRetainedState(instanceId, kind, initial = null) {
            observeInstalledContent(kind, instanceId).map { outcome -> outcome.valueOrNull?.map { it.toView() } }
        }
        val (promptRemoveConfirmation, setPromptRemoveConfirmation) =
            remember { mutableStateOf<PendingContentRemoval?>(null) }

        return InstalledContentState(
            files = files,
            promptRemoveConfirmation = promptRemoveConfirmation,
        ) + { event ->
            handleEvent(
                event = event,
                promptRemoveConfirmation = promptRemoveConfirmation,
                setPromptRemoveConfirmation = setPromptRemoveConfirmation,
            )
        }
    }

    private fun handleEvent(
        event: InstalledContentEvent,
        promptRemoveConfirmation: PendingContentRemoval?,
        setPromptRemoveConfirmation: (PendingContentRemoval?) -> Unit,
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
                val imported = importContent(kind, instanceId, event.sourcePath)
                if (!imported) snackbarService.error { stringResource(InstanceRes.string.errorImportFailed) }
            }
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
