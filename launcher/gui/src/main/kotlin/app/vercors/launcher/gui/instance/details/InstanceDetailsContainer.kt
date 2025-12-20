package app.vercors.launcher.gui.instance.details

import app.vercors.launcher.common.domain.instance.model.Instance
import app.vercors.launcher.common.domain.instance.model.InstanceId
import app.vercors.launcher.common.domain.instance.usecase.ObserveInstanceListUseCase
import app.vercors.shared.domain.Resource
import app.vercors.shared.domain.map
import app.vercors.shared.ui.configureWithLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.map
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed

private val logger = KotlinLogging.logger {}

class InstanceDetailsContainer(
    private val instanceId: InstanceId,
    private val observeInstanceListUseCase: ObserveInstanceListUseCase
) : Container<InstanceDetailsState, InstanceDetailsIntent, InstanceDetailsAction> {
    override val store = store(initial = InstanceDetailsState()) {
        configureWithLogger(logger)

        reduce { intent ->
            when (intent) {
                else -> {}
            }
        }

        whileSubscribed {
            observeInstanceListUseCase()
                .map { instances -> instances.map { list -> list.first { it.id == instanceId }.toView() } }
                .collect { instance ->
                    updateState { copy(instance = instance) }
                }
        }
    }
}

data class InstanceDetailsState(
    val instance: Resource<InstanceView> = Resource.Unloaded,
) : MVIState

data class InstanceView(
    val id: InstanceId,
    val name: String,
    val version: String,
    val loader: String,
    val type: String,
)

fun Instance.toView(): InstanceView = InstanceView(
    id = id,
    name = name,
    version = gameVersion,
    loader = loaderName,
    type = typeName,
)

sealed interface InstanceDetailsIntent : MVIIntent {
    data object LaunchInstance : InstanceDetailsIntent
    data object DeleteInstance : InstanceDetailsIntent
}

sealed interface InstanceDetailsAction : MVIAction {
}
