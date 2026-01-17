package app.vercors.feature.instance.ui.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.vercors.core.domain.mapNullable
import app.vercors.core.ui.state.collectResourceAsStateWithLifecycle
import app.vercors.feature.instance.domain.usecase.DeleteInstanceUseCase
import app.vercors.feature.instance.domain.usecase.ObserveInstanceListUseCase
import app.vercors.feature.instance.domain.usecase.ObserveInstanceLogsUseCase
import app.vercors.feature.launcher.domain.usecase.CancelInstanceUseCase
import app.vercors.feature.launcher.domain.usecase.LaunchInstanceUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.plus
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

@AssistedInject
class InstanceDetailsPresenter(
    @Assisted private val screen: InstanceDetailsScreen,
    private val globalScope: CoroutineScope,
    private val observeInstanceList: ObserveInstanceListUseCase,
    private val observeInstanceLogs: ObserveInstanceLogsUseCase,
    private val launchInstance: LaunchInstanceUseCase,
    private val cancelInstance: CancelInstanceUseCase,
    private val deleteInstance: DeleteInstanceUseCase,
) : Presenter<InstanceDetailsState, InstanceDetailsEvent> {
    @Composable
    override fun present(): Store<InstanceDetailsState, InstanceDetailsEvent> {
        val scope = rememberCoroutineScope()
        val instance by remember(observeInstanceList, screen) {
            observeInstanceList()
                .map { res -> res.mapNullable { list -> list.find { it.instance.id == screen.instanceId }?.toView() } }
        }.collectResourceAsStateWithLifecycle()
        val logs by remember(observeInstanceLogs, screen) {
            observeInstanceLogs(screen.instanceId)
                .map { list -> list?.map { it.toView() } }
        }.collectAsStateWithLifecycle(null)
        return InstanceDetailsState(
            instance = instance,
            logs = logs
        ) + {
            when (it) {
                InstanceDetailsEvent.Launch -> scope.launch { launchInstance(screen.instanceId) }
                InstanceDetailsEvent.Delete -> globalScope.launch { deleteInstance(screen.instanceId) }
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
