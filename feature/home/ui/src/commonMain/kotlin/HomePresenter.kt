package app.vercors.feature.home.ui

import androidx.compose.runtime.*
import app.vercors.feature.home.domain.usecase.HomeSection
import app.vercors.feature.home.domain.usecase.ObserveHomeSectionListUseCase
import app.vercors.feature.instance.ui.actions.InstanceActionsEvent
import app.vercors.feature.instance.ui.actions.InstanceActionsPresenter
import app.vercors.feature.instance.ui.details.InstanceDetailsScreen
import app.vercors.feature.instance.ui.toBriefView
import app.vercors.feature.project.domain.usecase.ObserveInstanceUpdatesUseCase
import app.vercors.feature.project.ui.details.ProjectDetailsScreen
import app.vercors.feature.project.ui.install.*
import app.vercors.feature.project.ui.toView
import app.vercors.shared.ui.*
import arrow.core.raise.context.either
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceAndCollectAsRetainedState
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import opensavvy.state.progressive.map

@AssistedInject
class HomePresenter(
    @Assisted private val navigator: Navigator,
    private val observeHomeSectionList: ObserveHomeSectionListUseCase,
    private val observeInstanceUpdates: ObserveInstanceUpdatesUseCase,
    private val snackbarService: SnackbarService,
    private val installProjectDialogFactory: InstallProjectDialog.Factory,
    private val installModpackDialogFactory: InstallModpackDialog.Factory,
    private val instanceActionsPresenterFactory: InstanceActionsPresenter.Factory,
) : Presenter<HomeState, HomeEvent> {
    @Composable
    override fun present(): Store<HomeState, HomeEvent> {
        val sections by observeHomeState()
        val instanceActions = remember { instanceActionsPresenterFactory.create(navigator) }.present()
        val (installTarget, setInstallTarget) = remember { mutableStateOf<ProjectInstallTarget?>(null) }

        return HomeState(
            sections = sections,
            instanceActions = instanceActions,
            installTarget = installTarget,
        ) + { event ->
            handleEvent(
                event = event,
                instanceActionsSink = instanceActions.eventSink,
                setInstallTarget = setInstallTarget,
            )
        }
    }

    private fun handleEvent(
        event: HomeEvent,
        instanceActionsSink: EventSink<InstanceActionsEvent>,
        setInstallTarget: (ProjectInstallTarget?) -> Unit,
    ) {
        when (event) {
            is HomeEvent.OnInstanceAction -> instanceActionsSink(event.event)
            is HomeEvent.OnProjectClick -> navigator.goTo(ProjectDetailsScreen(event.projectId))
            is HomeEvent.OnInstallProjectClick -> setInstallTarget(
                event.project.toInstallTarget(
                    preferredInstanceId = null,
                    installProjectDialogFactory = installProjectDialogFactory,
                    installModpackDialogFactory = installModpackDialogFactory,
                )
            )

            is HomeEvent.OnInstallResult -> {
                setInstallTarget(null)
                if (event.result is InstallProjectResult.GoToInstance) navigator.goTo(InstanceDetailsScreen(event.result.instanceId))
            }

            is HomeEvent.OnInstallModpackResult -> {
                setInstallTarget(null)
                if (event.result is InstallModpackResult.Installed) navigator.goTo(InstanceDetailsScreen(event.result.instanceId))
            }
        }
    }

    @Composable
    private fun observeHomeState(): State<List<HomeState.Section<out Any>>> =
        produceAndCollectAsRetainedState(initial = emptyList()) {
            either { observeHomeSectionList() }.fold(
                ifLeft = {
                    snackbarService.error { "A configuration error occurred: $it" }
                    flowOf(emptyList())
                },
                ifRight = { flow ->
                    combine(flow, observeInstanceUpdates()) { list, updates ->
                        list.map { section ->
                            when (section) {
                                is HomeSection.InstanceListSection -> HomeState.Section.InstanceList(
                                    type = section.type,
                                    data = section.data.map { list ->
                                        list.map {
                                            it.toBriefView(updateAvailable = updates[it.instance.id]?.hasUpdates == true)
                                        }
                                    }
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
