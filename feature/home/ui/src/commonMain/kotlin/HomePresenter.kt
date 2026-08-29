package app.vercors.feature.home.ui

import androidx.compose.runtime.*
import app.vercors.feature.home.domain.usecase.HomeSection
import app.vercors.feature.home.domain.usecase.ObserveHomeSectionListUseCase
import app.vercors.feature.instance.ui.create.CreateInstanceDialog
import app.vercors.feature.instance.ui.create.CreateInstanceResult
import app.vercors.feature.instance.ui.details.InstanceDetailsScreen
import app.vercors.feature.instance.ui.toBriefView
import app.vercors.feature.project.ui.details.ProjectDetailsScreen
import app.vercors.feature.project.ui.toView
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.Store
import arrow.core.raise.context.either
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceAndCollectAsRetainedState
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.map
import opensavvy.state.progressive.map

@AssistedInject
class HomePresenter(
    @Assisted val navigator: Navigator,
    val observeHomeSectionList: ObserveHomeSectionListUseCase,
    val createInstanceDialog: CreateInstanceDialog
) : Presenter<HomeState, HomeEvent> {
    @Composable
    override fun present(): Store<HomeState, HomeEvent> {
        val sections by observeHomeState()
        val (promptCreateInstance, setPromptCreateInstance) = remember { mutableStateOf(false) }

        return HomeState(
            sections = sections,
            promptCreateInstance = promptCreateInstance,
            createInstanceDialog = createInstanceDialog
        ) + { event ->
            handleEvent(
                event = event,
                setPromptCreateInstance = setPromptCreateInstance
            )
        }
    }

    private fun handleEvent(event: HomeEvent, setPromptCreateInstance: (Boolean) -> Unit) {
        when (event) {
            HomeEvent.OnCreateInstanceClick -> setPromptCreateInstance(true)
            is HomeEvent.OnCreateInstanceResult -> {
                setPromptCreateInstance(false)
                val result = event.result
                if (result is CreateInstanceResult.Created) navigator.goTo(InstanceDetailsScreen(result.instanceId))
            }

            is HomeEvent.OnProjectClick -> navigator.goTo(ProjectDetailsScreen(event.projectId))
            is HomeEvent.OnInstanceClick -> navigator.goTo(InstanceDetailsScreen(event.instanceId))
        }
    }

    @Composable
    private fun observeHomeState(): State<List<HomeState.Section<out Any>>> =
        produceAndCollectAsRetainedState(initial = emptyList()) {
            either { observeHomeSectionList() }.fold(
                ifLeft = { error("A configuration error occurred: $it") }, // TODO check if error handling can be better here
                ifRight = { flow ->
                    flow.map { list ->
                        list.map { section ->
                            when (section) {
                                is HomeSection.InstanceListSection -> HomeState.Section.InstanceList(
                                    type = section.type,
                                    data = section.data.map { list -> list.map { it.toBriefView() } }
                                )

                                is HomeSection.ProjectListSection -> HomeState.Section.ProjectList(
                                    type = section.type,
                                    data = section.data.map { list -> list.map { it.toView() } }
                                )
                            }
                        }
                    }
                }
            )
        }

    @CircuitInject(HomeScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(navigator: Navigator): HomePresenter
    }
}
