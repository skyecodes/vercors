package app.vercors.feature.instance.ui.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import app.vercors.core.ui.state.produceAndCollectAsProgressiveState
import app.vercors.feature.instance.domain.model.InstanceError
import app.vercors.feature.instance.domain.usecase.DeleteInstanceUseCase
import app.vercors.feature.instance.domain.usecase.ObserveInstanceLogsUseCase
import app.vercors.feature.instance.domain.usecase.ObserveInstanceUseCase
import app.vercors.feature.instance.ui.message
import app.vercors.feature.launcher.domain.usecase.CancelInstanceUseCase
import app.vercors.feature.launcher.domain.usecase.KillInstanceUseCase
import app.vercors.feature.launcher.domain.usecase.LaunchInstanceUseCase
import app.vercors.feature.launcher.domain.usecase.StopInstanceUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.error
import arrow.core.raise.context.either
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceAndCollectAsRetainedState
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.map

@AssistedInject
class InstanceDetailsPresenter(
    @Assisted private val screen: InstanceDetailsScreen,
    private val globalScope: CoroutineScope,
    private val observeInstance: ObserveInstanceUseCase,
    private val observeInstanceLogs: ObserveInstanceLogsUseCase,
    private val launchInstance: LaunchInstanceUseCase,
    private val cancelInstance: CancelInstanceUseCase,
    private val stopInstance: StopInstanceUseCase,
    private val killInstance: KillInstanceUseCase,
    private val deleteInstance: DeleteInstanceUseCase,
    private val snackbarService: SnackbarService
) : Presenter<InstanceDetailsState, InstanceDetailsEvent> {
    @Composable
    override fun present(): Store<InstanceDetailsState, InstanceDetailsEvent> {
        val instance by observeInstanceView()
        val logs by observeInstanceLogsView()
        return InstanceDetailsState(
            instance = instance,
            logs = logs
        ) + ::handleEvent

    }

    private fun handleEvent(event: InstanceDetailsEvent) = when (event) {
        InstanceDetailsEvent.Launch -> globalScope.launch {
            either { launchInstance(screen.instanceId) }.onLeft { error -> snackbarService.error { error.message } }
        }

        InstanceDetailsEvent.Delete -> globalScope.launch {
            either { deleteInstance(screen.instanceId) }.onLeft { error -> snackbarService.error { error.message } }
        }

        InstanceDetailsEvent.Cancel -> cancelInstance(screen.instanceId)
        InstanceDetailsEvent.Stop -> globalScope.launch { stopInstance(screen.instanceId) }
        InstanceDetailsEvent.Kill -> globalScope.launch { killInstance(screen.instanceId) }
    }


    @Composable
    private fun observeInstanceView(): State<ProgressiveOutcome<InstanceError, InstanceView>> =
        produceAndCollectAsProgressiveState(screen.instanceId) {
            observeInstance(screen.instanceId).map { outcome -> outcome.map { it.toView() } }
        }

    @Composable
    private fun observeInstanceLogsView(): State<List<InstanceLogEventView>?> =
        produceAndCollectAsRetainedState(observeInstanceLogs, screen, initial = null) {
            observeInstanceLogs(screen.instanceId).map { list -> list?.map { it.toView() } }
        }

    @CircuitInject(InstanceDetailsScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(screen: InstanceDetailsScreen): InstanceDetailsPresenter
    }
}
