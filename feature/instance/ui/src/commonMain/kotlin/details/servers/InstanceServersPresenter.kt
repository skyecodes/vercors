package app.vercors.feature.instance.ui.details.servers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.usecase.ObserveInstanceServersUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.Store
import com.slack.circuit.retained.produceAndCollectAsRetainedState
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.map
import opensavvy.state.progressive.valueOrNull

@AssistedInject
class InstanceServersPresenter(
    @Assisted private val instanceId: InstanceId,
    private val observeServers: ObserveInstanceServersUseCase,
) : Presenter<InstanceServersState, InstanceServersEvent> {
    @Composable
    override fun present(): Store<InstanceServersState, InstanceServersEvent> {
        val servers by produceAndCollectAsRetainedState(instanceId, initial = null) {
            observeServers(instanceId).map { outcome -> outcome.valueOrNull?.map { it.toView() } }
        }

        return InstanceServersState(servers = servers) + { }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(instanceId: InstanceId): InstanceServersPresenter
    }
}
