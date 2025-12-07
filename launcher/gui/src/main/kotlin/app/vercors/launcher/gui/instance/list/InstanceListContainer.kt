package app.vercors.launcher.gui.instance.list

import app.vercors.launcher.common.domain.instance.usecase.ObserveInstanceListUseCase
import app.vercors.shared.domain.Resource
import app.vercors.shared.domain.Result
import app.vercors.shared.ui.configureWithLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.logging.logt
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed

private val logger = KotlinLogging.logger {}

class InstanceListContainer(
    val observeInstanceListUseCase: ObserveInstanceListUseCase
) : Container<InstanceListState, InstanceListIntent, InstanceListAction> {
    override val store = store(initial = InstanceListState.Loading) {
        configureWithLogger(logger) {
            debuggable = true
            name = "InstanceListContainer"
        }

        reduce { intent ->
            logt { "Intent: $intent" }
            when (intent) {
                is InstanceListIntent.OpenInstanceClick -> action(InstanceListAction.NavigateToInstanceDetails(intent.instanceId))
            }
        }

        whileSubscribed {
            observeInstanceListUseCase().collect { instances ->
                logt { "InstanceList: $instances" }
                updateState {
                    when (instances) {
                        Resource.Unloaded, Resource.Loading -> InstanceListState.Loading
                        is Resource.Loaded -> when (val result = instances.result) {
                            is Result.Error -> InstanceListState.Error(result.throwable)
                            is Result.Success -> InstanceListState.Loaded(result.data.map { InstanceView(it.name) })
                        }
                    }
                }
            }
        }
    }
}

sealed interface InstanceListState : MVIState {
    data object Loading : InstanceListState
    data class Loaded(val instances: List<InstanceView>) : InstanceListState
    data class Error(val throwable: Throwable) : InstanceListState
}

sealed interface InstanceListIntent : MVIIntent {
    data class OpenInstanceClick(val instanceId: String) : InstanceListIntent
}

sealed interface InstanceListAction : MVIAction {
    data class NavigateToInstanceDetails(val instanceId: String) : InstanceListAction
}

data class InstanceView(
    val name: String
)
