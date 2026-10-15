package app.vercors.feature.instance.ui.copy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import app.vercors.feature.instance.domain.usecase.CopyInstanceUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.error
import arrow.core.raise.context.either
import com.slack.circuit.overlay.OverlayNavigator
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AssistedInject
class CopyInstancePresenter(
    @Assisted private val target: CopyInstanceTarget,
    @Assisted private val navigator: OverlayNavigator<CopyInstanceResult>,
    private val globalScope: CoroutineScope,
    private val copyInstance: CopyInstanceUseCase,
    private val snackbarService: SnackbarService,
) : Presenter<CopyInstanceState, CopyInstanceEvent> {
    @Composable
    override fun present(): Store<CopyInstanceState, CopyInstanceEvent> {
        val (instanceName, setInstanceName) = remember { mutableStateOf("${target.name} (Copy)") }
        val (isCopying, setIsCopying) = remember { mutableStateOf(false) }

        return CopyInstanceState(
            instanceName = instanceName,
            isCopying = isCopying,
        ) + { event ->
            handleEvent(
                event = event,
                instanceName = instanceName,
                setInstanceName = setInstanceName,
                setIsCopying = setIsCopying,
            )
        }
    }

    private fun handleEvent(
        event: CopyInstanceEvent,
        instanceName: String,
        setInstanceName: (String) -> Unit,
        setIsCopying: (Boolean) -> Unit,
    ) {
        when (event) {
            is CopyInstanceEvent.OnInstanceNameChange -> setInstanceName(event.name)
            CopyInstanceEvent.OnCancelClick -> navigator.finish(CopyInstanceResult.Cancelled)
            CopyInstanceEvent.OnCopyClick -> copyInstance(instanceName, setIsCopying)
        }
    }

    private fun copyInstance(instanceName: String, setIsCopying: (Boolean) -> Unit) {
        setIsCopying(true)
        globalScope.launch {
            either { copyInstance(target.id, instanceName) }
                .onLeft { error ->
                    setIsCopying(false)
                    snackbarService.error { error.toString() }
                }
                .onRight { copiedInstance -> navigator.finish(CopyInstanceResult.Copied(copiedInstance.id)) }
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            target: CopyInstanceTarget,
            navigator: OverlayNavigator<CopyInstanceResult>
        ): CopyInstancePresenter
    }
}
