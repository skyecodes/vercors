package app.vercors.feature.instance.ui.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import app.vercors.core.ui.state.produceAndCollectAsProgressiveState
import app.vercors.feature.game.domain.model.GameVersionError
import app.vercors.feature.game.domain.usecase.ObserveGameVersionListUseCase
import app.vercors.feature.game.ui.GameVersionView
import app.vercors.feature.game.ui.toView
import app.vercors.feature.instance.domain.model.InstanceError
import app.vercors.feature.instance.domain.usecase.DeleteInstanceUseCase
import app.vercors.feature.instance.domain.usecase.ObserveInstanceLogsUseCase
import app.vercors.feature.instance.domain.usecase.ObserveInstanceUseCase
import app.vercors.feature.instance.domain.usecase.UpdateInstanceUseCase
import app.vercors.feature.instance.ui.list.InstanceListScreen
import app.vercors.feature.instance.ui.message
import app.vercors.feature.launcher.domain.usecase.CancelInstanceUseCase
import app.vercors.feature.launcher.domain.usecase.KillInstanceUseCase
import app.vercors.feature.launcher.domain.usecase.LaunchInstanceUseCase
import app.vercors.feature.launcher.domain.usecase.StopInstanceUseCase
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
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.map
import opensavvy.state.progressive.valueOrNull

@AssistedInject
class InstanceDetailsPresenter(
    @Assisted private val screen: InstanceDetailsScreen,
    @Assisted private val navigator: Navigator,
    private val globalScope: CoroutineScope,
    private val observeInstance: ObserveInstanceUseCase,
    private val observeInstanceLogs: ObserveInstanceLogsUseCase,
    private val launchInstance: LaunchInstanceUseCase,
    private val cancelInstance: CancelInstanceUseCase,
    private val stopInstance: StopInstanceUseCase,
    private val killInstance: KillInstanceUseCase,
    private val deleteInstance: DeleteInstanceUseCase,
    private val snackbarService: SnackbarService,
    private val updateInstance: UpdateInstanceUseCase,
    private val observeGameVersionList: ObserveGameVersionListUseCase
) : Presenter<InstanceDetailsState, InstanceDetailsEvent> {
    @Composable
    override fun present(): Store<InstanceDetailsState, InstanceDetailsEvent> {
        val instance by observeInstanceView()
        val gameVersionList by observeGameVersionListView()
        val logs by observeInstanceLogsView()
        val (promptDeleteConfirmation, setPromptDeleteConfirmation) = rememberSaveable { mutableStateOf(false) }
        val (selectedTab, setSelectedTab) = rememberSaveable { mutableStateOf(InstanceDetailsTab.Options) }
        val (editedInstance, setEditedInstance) = rememberSaveable { mutableStateOf(InstanceEditState()) }

        return InstanceDetailsState(
            instance = instance,
            editedInstance = editedInstance,
            gameVersionList = gameVersionList.valueOrNull ?: emptyList(),
            promptDeleteConfirmation = promptDeleteConfirmation,
            selectedTab = selectedTab,
            logs = logs
        ) + { event ->
            handleEvent(
                event = event,
                setPromptDeleteConfirmation = setPromptDeleteConfirmation,
                setSelectedTab = setSelectedTab,
                setEditedInstance = setEditedInstance,
                editedInstance = editedInstance
            )
        }
    }

    private fun handleEvent(
        event: InstanceDetailsEvent,
        setPromptDeleteConfirmation: (Boolean) -> Unit,
        setSelectedTab: (InstanceDetailsTab) -> Unit,
        setEditedInstance: (InstanceEditState) -> Unit,
        editedInstance: InstanceEditState
    ) {
        when (event) {
            InstanceDetailsEvent.Launch -> globalScope.launch {
                setSelectedTab(InstanceDetailsTab.Logs)
                either { launchInstance(screen.instanceId) }.onLeft { error -> snackbarService.error { error.message } }
            }

            InstanceDetailsEvent.Delete -> setPromptDeleteConfirmation(true)
            is InstanceDetailsEvent.ConfirmDelete -> {
                setPromptDeleteConfirmation(false)
                if (event.result) globalScope.launch {
                    either { deleteInstance(screen.instanceId) }
                        .onLeft { error -> snackbarService.error { error.message } }
                        .onRight {
                            navigator.pop()
                            navigator.goTo(InstanceListScreen)
                        }
                }
            }

            InstanceDetailsEvent.Cancel -> cancelInstance(screen.instanceId)
            InstanceDetailsEvent.Stop -> globalScope.launch { stopInstance(screen.instanceId) }
            InstanceDetailsEvent.Kill -> globalScope.launch { killInstance(screen.instanceId) }

            is InstanceDetailsEvent.SelectTab -> setSelectedTab(event.tab)
            is InstanceDetailsEvent.UpdateInstance -> setEditedInstance(event.update(editedInstance))
            InstanceDetailsEvent.CancelEdit -> setEditedInstance(InstanceEditState())
            InstanceDetailsEvent.SaveInstance -> globalScope.launch {
                either {
                    updateInstance(screen.instanceId) {
                        var updatedInstance = it
                        editedInstance.name?.let { name -> updatedInstance = updatedInstance.copy(name = name) }
                        editedInstance.gameVersion?.let { version ->
                            updatedInstance = updatedInstance.copy(gameVersion = version)
                        }
                        updatedInstance
                    }
                    setEditedInstance(InstanceEditState())
                }.onLeft { error -> snackbarService.error { error.toString() } }
            }
        }
    }


    @Composable
    private fun observeInstanceView(): State<ProgressiveOutcome<InstanceError, InstanceView>> =
        produceAndCollectAsProgressiveState(screen.instanceId) {
            observeInstance(screen.instanceId).map { outcome -> outcome.map { it.toView() } }
        }

    @Composable
    private fun observeInstanceLogsView(): State<List<InstanceLogEventView>?> =
        produceAndCollectAsRetainedState(observeInstanceLogs, screen, initial = null) {
            observeInstanceLogs(screen.instanceId).map { list -> list?.map { it.toView() } }
        }

    @Composable
    private fun observeGameVersionListView(): State<ProgressiveOutcome<GameVersionError.InvalidManifest, List<GameVersionView>>> =
        produceAndCollectAsProgressiveState {
            observeGameVersionList().map { res -> res.map { it.toView() } }
        }

    @CircuitInject(InstanceDetailsScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(screen: InstanceDetailsScreen, navigator: Navigator): InstanceDetailsPresenter
    }
}
