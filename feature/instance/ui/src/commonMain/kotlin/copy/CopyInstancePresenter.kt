package app.vercors.feature.instance.ui.copy

import androidx.compose.runtime.*
import app.vercors.feature.instance.domain.usecase.CopyInstanceUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.error
import arrow.core.raise.context.either
import com.slack.circuit.overlay.OverlayNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Not `@CircuitInject`-wired since [CopyInstanceDialog] isn't a navigated [Screen][app.vercors.shared.ui.Screen] —
 * it's constructed manually by the dialog for each show, mirroring [CreateInstancePresenter][app.vercors.feature.instance.ui.create.CreateInstancePresenter].
 */
class CopyInstancePresenter(
    private val target: CopyInstanceTarget,
    private val navigator: OverlayNavigator<CopyInstanceResult>,
    private val globalScope: CoroutineScope,
    private val copyInstance: CopyInstanceUseCase,
    private val snackbarService: SnackbarService,
) : Presenter<CopyInstanceState, CopyInstanceEvent> {
    @Composable
    override fun present(): Store<CopyInstanceState, CopyInstanceEvent> {
        val (instanceName, setInstanceName) = remember { mutableStateOf("${target.name} (Copy)") }
        var isCopying by remember { mutableStateOf(false) }

        return CopyInstanceState(
            instanceName = instanceName,
            isCopying = isCopying,
        ) + { event ->
            handleEvent(
                event = event,
                instanceName = instanceName,
                setInstanceName = setInstanceName,
                setIsCopying = { isCopying = it },
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
}
