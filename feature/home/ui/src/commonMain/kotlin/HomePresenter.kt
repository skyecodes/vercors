package app.vercors.feature.home.ui

import androidx.compose.runtime.*
import app.vercors.feature.home.domain.usecase.HomeSection
import app.vercors.feature.home.domain.usecase.ObserveHomeSectionListUseCase
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.ui.create.CreateInstanceDialog
import app.vercors.feature.instance.ui.create.CreateInstanceResult
import app.vercors.feature.instance.ui.details.InstanceDetailsScreen
import app.vercors.feature.instance.ui.details.InstanceDetailsTab
import app.vercors.feature.instance.ui.message
import app.vercors.feature.instance.ui.toBriefView
import app.vercors.feature.launcher.domain.model.InstanceLaunchError
import app.vercors.feature.launcher.domain.usecase.KillInstanceUseCase
import app.vercors.feature.launcher.domain.usecase.LaunchInstanceUseCase
import app.vercors.feature.launcher.domain.usecase.StopInstanceUseCase
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.ui.details.ProjectDetailsScreen
import app.vercors.feature.project.ui.list.ProjectListScreen
import app.vercors.feature.project.ui.toView
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.error
import arrow.core.raise.context.either
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceAndCollectAsRetainedState
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import opensavvy.state.progressive.map

@AssistedInject
class HomePresenter(
    @Assisted val navigator: Navigator,
    val observeHomeSectionList: ObserveHomeSectionListUseCase,
    val createInstanceDialog: CreateInstanceDialog,
    private val globalScope: CoroutineScope,
    private val launchInstance: LaunchInstanceUseCase,
    private val stopInstance: StopInstanceUseCase,
    private val killInstance: KillInstanceUseCase,
    private val snackbarService: SnackbarService,
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
                if (result is CreateInstanceResult.Created) navigator.goTo(InstanceDetailsScreen(event.result.instanceId))
                else if (result == CreateInstanceResult.SearchModpacks) navigator.goTo(ProjectListScreen(initialType = ProjectType.Modpack))
            }

            is HomeEvent.OnProjectClick -> navigator.goTo(ProjectDetailsScreen(event.projectId))
            is HomeEvent.OnInstanceClick -> navigator.goTo(InstanceDetailsScreen(event.instanceId))
            is HomeEvent.OnInstanceLaunchClick -> handleLaunchClick(event.instanceId)
            is HomeEvent.OnInstanceStopClick -> globalScope.launch { stopInstance(event.instanceId) }
            is HomeEvent.OnInstanceKillClick -> globalScope.launch { killInstance(event.instanceId) }
        }
    }

    private fun handleLaunchClick(instanceId: InstanceId) = globalScope.launch {
        either { launchInstance(instanceId) }.onLeft { error ->
            when (error) {
                is InstanceLaunchError.NoCompatibleJava, InstanceLaunchError.LoginRequired ->
                    navigator.goTo(
                        InstanceDetailsScreen(
                            instanceId,
                            initialTab = InstanceDetailsTab.Logs,
                            autoLaunch = true
                        )
                    )

                else -> snackbarService.error { error.message }
            }
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
