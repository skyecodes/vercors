package app.vercors.feature.instance.ui.details

import androidx.compose.runtime.*
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.usecase.*
import app.vercors.feature.instance.ui.InstanceRes
import app.vercors.feature.instance.ui.errorImportFailed
import app.vercors.shared.ui.*
import com.slack.circuit.retained.produceAndCollectAsRetainedState
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import opensavvy.state.progressive.valueOrNull
import org.jetbrains.compose.resources.stringResource

@Immutable
data class InstalledContentState(
    val files: List<InstalledContentFileView>?,
    val promptRemoveConfirmation: PendingContentRemoval?,
)

@Immutable
data class PendingContentRemoval(
    val fileName: String,
    val title: String,
)

@Immutable
sealed interface InstalledContentEvent : Event {
    data class OnEnabledChange(val fileName: String, val enabled: Boolean) : InstalledContentEvent
    data class OnRemoveClick(val fileName: String, val title: String) : InstalledContentEvent
    data class OnConfirmRemove(val result: Boolean) : InstalledContentEvent
    data class OnImportClick(val sourcePath: String) : InstalledContentEvent
}

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
            when (event) {
                is InstalledContentEvent.OnEnabledChange -> globalScope.launch {
                    setContentEnabled(kind, instanceId, event.fileName, event.enabled)
                }

                is InstalledContentEvent.OnRemoveClick ->
                    setPromptRemoveConfirmation(PendingContentRemoval(event.fileName, event.title))

                is InstalledContentEvent.OnConfirmRemove -> {
                    setPromptRemoveConfirmation(null)
                    if (event.result && promptRemoveConfirmation != null) {
                        globalScope.launch { removeContent(kind, instanceId, promptRemoveConfirmation.fileName) }
                    }
                }

                is InstalledContentEvent.OnImportClick -> globalScope.launch {
                    val imported = importContent(kind, instanceId, event.sourcePath)
                    if (!imported) snackbarService.error { stringResource(InstanceRes.string.errorImportFailed) }
                }
            }
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(instanceId: InstanceId, kind: ContentKind): InstalledContentPresenter
    }
}
