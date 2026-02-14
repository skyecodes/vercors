package app.vercors.feature.instance.ui.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import app.vercors.core.ui.state.collectProgressiveAsState
import app.vercors.feature.instance.domain.usecase.DeleteInstanceUseCase
import app.vercors.feature.instance.domain.usecase.ObserveInstanceLogsUseCase
import app.vercors.feature.instance.domain.usecase.ObserveInstanceUseCase
import app.vercors.feature.instance.ui.message
import app.vercors.feature.launcher.domain.usecase.CancelInstanceUseCase
import app.vercors.feature.launcher.domain.usecase.LaunchInstanceUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.error
import arrow.core.raise.context.either
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.collectAsRetainedState
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import opensavvy.state.progressive.map

private val logger = KotlinLogging.logger {}

@AssistedInject
class InstanceDetailsPresenter(
    @Assisted private val screen: InstanceDetailsScreen,
    private val globalScope: CoroutineScope,
    private val observeInstance: ObserveInstanceUseCase,
    private val observeInstanceLogs: ObserveInstanceLogsUseCase,
    private val launchInstance: LaunchInstanceUseCase,
    private val cancelInstance: CancelInstanceUseCase,
    private val deleteInstance: DeleteInstanceUseCase,
    private val snackbarService: SnackbarService
) : Presenter<InstanceDetailsState, InstanceDetailsEvent> {
    @Composable
    override fun present(): Store<InstanceDetailsState, InstanceDetailsEvent> {
        val scope = rememberCoroutineScope()
        val instance by remember(observeInstance, screen) {
            observeInstance(screen.instanceId).map { outcome -> outcome.map { it.toView() } }
        }.collectProgressiveAsState()
        val logs by remember(observeInstanceLogs, screen) {
            observeInstanceLogs(screen.instanceId)
                .map { list -> list?.map { it.toView() } }
        }.collectAsRetainedState(null)
        return InstanceDetailsState(
            instance = instance,
            logs = logs
        ) + {
            when (it) {
                InstanceDetailsEvent.Launch -> scope.launch {
                    either { launchInstance(screen.instanceId) }
                        .onLeft { error -> snackbarService.error { error.message } }
                }

                InstanceDetailsEvent.Delete -> globalScope.launch {
                    either { deleteInstance(screen.instanceId) }
                        .onLeft { error -> snackbarService.error { error.message } }
                }
                is InstanceDetailsEvent.Cancel -> cancelInstance(screen.instanceId)
            }
        }
    }

    @CircuitInject(InstanceDetailsScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(screen: InstanceDetailsScreen): InstanceDetailsPresenter
    }
}
