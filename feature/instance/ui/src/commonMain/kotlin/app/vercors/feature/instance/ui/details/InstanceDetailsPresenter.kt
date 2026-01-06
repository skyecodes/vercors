package app.vercors.feature.instance.ui.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import app.vercors.core.domain.map
import app.vercors.core.ui.state.collectResourceAsStateWithLifecycle
import app.vercors.feature.instance.domain.usecase.DeleteInstanceUseCase
import app.vercors.feature.instance.domain.usecase.ObserveInstanceListUseCase
import app.vercors.feature.launcher.domain.usecase.LaunchInstanceUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.plus
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AssistedInject
class InstanceDetailsPresenter(
    @Assisted private val screen: InstanceDetailsScreen,
    private val globalScope: CoroutineScope,
    private val observeInstanceList: ObserveInstanceListUseCase,
    private val launchInstance: LaunchInstanceUseCase,
    private val deleteInstance: DeleteInstanceUseCase,
) : Presenter<InstanceDetailsState, InstanceDetailsEvent> {
    @Composable
    override fun present(): Store<InstanceDetailsState, InstanceDetailsEvent> {
        val instance by remember(observeInstanceList) {
            observeInstanceList()
                .map { res -> res.map { list -> list.first { it.id == screen.instanceId }.toView() } }
        }.collectResourceAsStateWithLifecycle()
        return InstanceDetailsState(
            instance = instance
        ) + {
            when (it) {
                InstanceDetailsEvent.Launch -> globalScope.launch { launchInstance(screen.instanceId).collect() }
                InstanceDetailsEvent.Delete -> globalScope.launch { deleteInstance(screen.instanceId) }
            }
        }
    }

    @CircuitInject(InstanceDetailsScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(screen: InstanceDetailsScreen): InstanceDetailsPresenter
    }
}
