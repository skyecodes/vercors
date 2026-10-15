package app.vercors.feature.instance.ui.list

import androidx.compose.runtime.*
import app.vercors.core.domain.filterIsRight
import app.vercors.core.ui.state.produceAndCollectAsProgressiveState
import app.vercors.feature.instance.domain.usecase.ObserveInstanceListUseCase
import app.vercors.feature.instance.ui.BriefInstanceView
import app.vercors.feature.instance.ui.actions.InstanceActionsPresenter
import app.vercors.feature.instance.ui.toBriefView
import app.vercors.feature.project.domain.usecase.ObserveInstanceUpdatesUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.Store
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.combine
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.map

@AssistedInject
class InstanceListPresenter(
    @Assisted private val navigator: Navigator,
    private val observeInstanceList: ObserveInstanceListUseCase,
    private val observeInstanceUpdates: ObserveInstanceUpdatesUseCase,
    private val instanceActionsPresenterFactory: InstanceActionsPresenter.Factory,
) : Presenter<InstanceListState, InstanceListEvent> {
    @Composable
    override fun present(): Store<InstanceListState, InstanceListEvent> {
        val instances by observeInstanceListView()
        val instanceActions = remember { instanceActionsPresenterFactory.create(navigator) }.present()
        val (nameFilter, setNameFilter) = remember { mutableStateOf("") }
        val (versionFilter, setVersionFilter) = remember { mutableStateOf("") }
        val (loaderFilter, setLoaderFilter) = remember { mutableStateOf("") }
        val (typeFilter, setTypeFilter) = remember { mutableStateOf("") }
        return InstanceListState(
            instances = instances,
            nameFilter = nameFilter,
            versionFilter = versionFilter,
            loaderFilter = loaderFilter,
            typeFilter = typeFilter,
            instanceActions = instanceActions,
        ) + { event ->
            handleEvent(
                event = event,
                setNameFilter = setNameFilter,
                setVersionFilter = setVersionFilter,
                setLoaderFilter = setLoaderFilter,
                setTypeFilter = setTypeFilter,
            )
        }
    }

    private fun handleEvent(
        event: InstanceListEvent,
        setNameFilter: (String) -> Unit,
        setVersionFilter: (String) -> Unit,
        setLoaderFilter: (String) -> Unit,
        setTypeFilter: (String) -> Unit,
    ) {
        when (event) {
            is InstanceListEvent.OnNameFilterChange -> setNameFilter(event.name)
            is InstanceListEvent.OnVersionFilterChange -> setVersionFilter(event.version)
            is InstanceListEvent.OnLoaderFilterChange -> setLoaderFilter(event.loader)
            is InstanceListEvent.OnTypeFilterChange -> setTypeFilter(event.type)
            InstanceListEvent.OnResetFilterClick -> {
                setNameFilter("")
                setVersionFilter("")
                setLoaderFilter("")
                setTypeFilter("")
            }
        }
    }

    @Composable
    private fun observeInstanceListView(): State<ProgressiveOutcome<Nothing, List<BriefInstanceView>>> =
        produceAndCollectAsProgressiveState {
            combine(observeInstanceList(), observeInstanceUpdates()) { res, updates ->
                res.map { list ->
                    list.filterIsRight().sortedByDescending { it.instance.lastPlayedAt }.map {
                        it.toBriefView(updateAvailable = updates[it.instance.id]?.hasUpdates == true)
                    }
                }
            }
        }

    @CircuitInject(InstanceListScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(navigator: Navigator): InstanceListPresenter
    }
}
