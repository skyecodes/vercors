package app.vercors.feature.instance.ui.list

import androidx.compose.runtime.*
import app.vercors.core.domain.filterIsRight
import app.vercors.core.ui.state.produceAndCollectAsProgressiveState
import app.vercors.feature.instance.domain.usecase.ObserveInstanceListUseCase
import app.vercors.feature.instance.ui.BriefInstanceView
import app.vercors.feature.instance.ui.create.CreateInstanceDialog
import app.vercors.feature.instance.ui.create.CreateInstanceResult
import app.vercors.feature.instance.ui.details.InstanceDetailsScreen
import app.vercors.feature.instance.ui.toBriefView
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.Store
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.map
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.map

@AssistedInject
class InstanceListPresenter(
    @Assisted private val navigator: Navigator,
    private val observeInstanceList: ObserveInstanceListUseCase,
    private val createInstanceDialog: CreateInstanceDialog
) : Presenter<InstanceListState, InstanceListEvent> {
    @Composable
    override fun present(): Store<InstanceListState, InstanceListEvent> {
        val instances by observeInstanceListView()
        val (nameFilter, setNameFilter) = remember { mutableStateOf("") }
        val (versionFilter, setVersionFilter) = remember { mutableStateOf("") }
        val (loaderFilter, setLoaderFilter) = remember { mutableStateOf("") }
        val (typeFilter, setTypeFilter) = remember { mutableStateOf("") }
        val (promptCreateInstance, setPromptCreateInstance) = remember { mutableStateOf(false) }
        return InstanceListState(
            instances = instances,
            nameFilter = nameFilter,
            versionFilter = versionFilter,
            loaderFilter = loaderFilter,
            typeFilter = typeFilter,
            promptCreateInstance = promptCreateInstance,
            createInstanceDialog = createInstanceDialog
        ) + { event ->
            handleEvent(
                event = event,
                setNameFilter = setNameFilter,
                setVersionFilter = setVersionFilter,
                setLoaderFilter = setLoaderFilter,
                setTypeFilter = setTypeFilter,
                setPromptCreateInstance = setPromptCreateInstance,
            )
        }
    }

    private fun handleEvent(
        event: InstanceListEvent,
        setNameFilter: (String) -> Unit,
        setVersionFilter: (String) -> Unit,
        setLoaderFilter: (String) -> Unit,
        setTypeFilter: (String) -> Unit,
        setPromptCreateInstance: (Boolean) -> Unit,
    ) {
        when (event) {
            InstanceListEvent.OnCreateInstanceClick -> setPromptCreateInstance(true)
            is InstanceListEvent.OnCreateInstanceResult -> {
                setPromptCreateInstance(false)
                val result = event.result
                if (result is CreateInstanceResult.Created) navigator.goTo(InstanceDetailsScreen(result.instanceId))
            }

            is InstanceListEvent.OnInstanceClick -> navigator.goTo(InstanceDetailsScreen(event.instanceId))
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
            observeInstanceList().map { res ->
                res.map { list -> list.filterIsRight().map { it.toBriefView() } }
            }
        }

    @CircuitInject(InstanceListScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(navigator: Navigator): InstanceListPresenter
    }
}
