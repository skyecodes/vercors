package app.vercors.feature.instance.ui.list

import app.vercors.core.domain.Resource
import app.vercors.core.domain.map
import app.vercors.core.domain.orNull
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.usecase.ObserveInstanceListUseCase
import app.vercors.feature.instance.ui.toBriefView
import app.vercors.shared.ui.configureWithLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed

private val logger = KotlinLogging.logger {}

class InstanceListContainer(
    val observeInstanceListUseCase: ObserveInstanceListUseCase
) : Container<InstanceListState, InstanceListIntent, InstanceListAction> {
    override val store = store(initial = InstanceListState()) {
        configureWithLogger(logger)

        reduce { intent ->
            when (intent) {
                InstanceListIntent.OnCreateInstanceClick -> action(InstanceListAction.NavigateToCreateInstance)
                is InstanceListIntent.OnNameFilterChange -> updateState { copy(nameFilter = intent.name) }
                is InstanceListIntent.OnTypeFilterChange -> updateState { copy(typeFilter = intent.type) }
                is InstanceListIntent.OnLoaderFilterChange -> updateState { copy(loaderFilter = intent.loader) }
                is InstanceListIntent.OnVersionFilterChange -> updateState { copy(versionFilter = intent.version) }
                is InstanceListIntent.OnInstanceClick -> action(InstanceListAction.NavigateToInstanceDetails(intent.instanceId))
                InstanceListIntent.OnResetFilterClick -> updateState {
                    copy(
                        nameFilter = "",
                        versionFilter = "",
                        loaderFilter = ""
                    )
                }
            }
        }

        whileSubscribed {
            observeInstanceListUseCase().collect { instances ->
                updateState {
                    copy(instances = instances.map { list -> list.map { it.toBriefView() } })
                }
            }
        }
    }
}

data class InstanceListState(
    val nameFilter: String = "",
    val versionFilter: String = "",
    val loaderFilter: String = "",
    val typeFilter: String = "",
    val instances: Resource<List<app.vercors.feature.instance.ui.BriefInstanceView>> = Resource.Unloaded
) : MVIState {
    val loadedInstances = instances.orNull() ?: emptyList()
    val filteredInstances = loadedInstances.filter {
        it.name.contains(nameFilter, ignoreCase = true) &&
                (versionFilter.isBlank() || it.version == versionFilter) &&
                (loaderFilter.isBlank() || it.loader == loaderFilter)
    }
    val versionList = listOf("") + loadedInstances.map { it.version }.distinct()
    val loaderList = listOf("") + loadedInstances.map { it.loader }.distinct()
    val typeList = listOf("") + loadedInstances.map { it.type }.distinct()
}

sealed interface InstanceListIntent : MVIIntent {
    data object OnCreateInstanceClick : InstanceListIntent
    data class OnNameFilterChange(val name: String) : InstanceListIntent
    data class OnTypeFilterChange(val type: String) : InstanceListIntent
    data class OnLoaderFilterChange(val loader: String) : InstanceListIntent
    data class OnVersionFilterChange(val version: String) : InstanceListIntent
    data class OnInstanceClick(val instanceId: InstanceId) : InstanceListIntent
    data object OnResetFilterClick : InstanceListIntent
}

sealed interface InstanceListAction : MVIAction {
    data object NavigateToCreateInstance : InstanceListAction
    data class NavigateToInstanceDetails(val instanceId: InstanceId) : InstanceListAction
}
