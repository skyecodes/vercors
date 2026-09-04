package app.vercors.feature.instance.ui.details

import androidx.compose.runtime.*
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.usecase.ObserveInstalledWorldsUseCase
import app.vercors.feature.instance.domain.usecase.RemoveWorldUseCase
import app.vercors.shared.ui.Event
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.Store
import com.slack.circuit.retained.produceAndCollectAsRetainedState
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import opensavvy.state.progressive.valueOrNull

@Immutable
data class InstalledWorldsState(
    val worlds: List<InstalledWorldView>?,
    val promptRemoveConfirmation: PendingWorldRemoval?,
)

@Immutable
data class PendingWorldRemoval(val folderName: String)

@Immutable
sealed interface InstalledWorldsEvent : Event {
    data class OnRemoveClick(val folderName: String) : InstalledWorldsEvent
    data class OnConfirmRemove(val result: Boolean) : InstalledWorldsEvent
}

@AssistedInject
class InstalledWorldsPresenter(
    @Assisted private val instanceId: InstanceId,
    private val observeInstalledWorlds: ObserveInstalledWorldsUseCase,
    private val removeWorld: RemoveWorldUseCase,
    private val globalScope: CoroutineScope,
) : Presenter<InstalledWorldsState, InstalledWorldsEvent> {
    @Composable
    override fun present(): Store<InstalledWorldsState, InstalledWorldsEvent> {
        val worlds by produceAndCollectAsRetainedState(instanceId, initial = null) {
            observeInstalledWorlds(instanceId).map { outcome -> outcome.valueOrNull?.map { it.toView() } }
        }
        val (promptRemoveConfirmation, setPromptRemoveConfirmation) =
            remember { mutableStateOf<PendingWorldRemoval?>(null) }

        return InstalledWorldsState(
            worlds = worlds,
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
        event: InstalledWorldsEvent,
        promptRemoveConfirmation: PendingWorldRemoval?,
        setPromptRemoveConfirmation: (PendingWorldRemoval?) -> Unit,
    ) {
        when (event) {
            is InstalledWorldsEvent.OnRemoveClick ->
                setPromptRemoveConfirmation(PendingWorldRemoval(event.folderName))

            is InstalledWorldsEvent.OnConfirmRemove ->
                handleConfirmRemove(event, promptRemoveConfirmation, setPromptRemoveConfirmation)
        }
    }

    private fun handleConfirmRemove(
        event: InstalledWorldsEvent.OnConfirmRemove,
        promptRemoveConfirmation: PendingWorldRemoval?,
        setPromptRemoveConfirmation: (PendingWorldRemoval?) -> Unit,
    ) {
        setPromptRemoveConfirmation(null)
        if (event.result && promptRemoveConfirmation != null) {
            globalScope.launch { removeWorld(instanceId, promptRemoveConfirmation.folderName) }
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(instanceId: InstanceId): InstalledWorldsPresenter
    }
}
