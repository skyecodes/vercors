package app.vercors.launcher.gui.instance.list

import app.vercors.launcher.common.domain.instance.usecase.GetInstanceListUseCase
import app.vercors.shared.domain.Resource
import app.vercors.shared.domain.Result
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed

class InstanceListContainer(
    val getInstanceListUseCase: GetInstanceListUseCase
) : Container<InstanceListState, InstanceListIntent, InstanceListAction> {
    override val store = store(initial = InstanceListState.Loading) {
        configure {
            debuggable = true
            name = "InstanceListContainer"
        }

        reduce { intent ->
            when (intent) {
                is InstanceListIntent.OpenInstanceClick -> action(InstanceListAction.NavigateToInstanceDetails(intent.instanceId))
            }
        }

        whileSubscribed {
            getInstanceListUseCase().collect { instances ->
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
