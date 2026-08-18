package app.vercors.feature.instance.ui.details

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import app.vercors.core.ui.state.mutableProgressiveStateOf
import app.vercors.core.ui.state.produceAndCollectAsProgressiveState
import app.vercors.feature.game.domain.model.GameVersionError
import app.vercors.feature.game.domain.model.LoaderError
import app.vercors.feature.game.domain.model.LoaderVersion
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.domain.usecase.GetAvailableLoadersUseCase
import app.vercors.feature.game.domain.usecase.GetLoaderVersionsUseCase
import app.vercors.feature.game.domain.usecase.ObserveGameVersionListUseCase
import app.vercors.feature.game.ui.GameVersionView
import app.vercors.feature.game.ui.toView
import app.vercors.feature.instance.domain.model.InstanceError
import app.vercors.feature.instance.domain.model.InstanceModLoader
import app.vercors.feature.instance.domain.usecase.*
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
import opensavvy.state.ExperimentalProgressiveRaiseApi
import opensavvy.state.arrow.progressive
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
    private val observeInstalledMods: ObserveInstalledModsUseCase,
    private val setModEnabled: SetModEnabledUseCase,
    private val launchInstance: LaunchInstanceUseCase,
    private val cancelInstance: CancelInstanceUseCase,
    private val stopInstance: StopInstanceUseCase,
    private val killInstance: KillInstanceUseCase,
    private val deleteInstance: DeleteInstanceUseCase,
    private val snackbarService: SnackbarService,
    private val updateInstance: UpdateInstanceUseCase,
    private val observeGameVersionList: ObserveGameVersionListUseCase,
    private val getAvailableLoaders: GetAvailableLoadersUseCase,
    private val getLoaderVersions: GetLoaderVersionsUseCase
) : Presenter<InstanceDetailsState, InstanceDetailsEvent> {
    @OptIn(ExperimentalProgressiveRaiseApi::class)
    @Composable
    override fun present(): Store<InstanceDetailsState, InstanceDetailsEvent> {
        val instance by observeInstanceView()
        val gameVersionList by observeGameVersionListView()
        val logs by observeInstanceLogsView()
        val installedMods by observeInstalledModsView()
        val (promptDeleteConfirmation, setPromptDeleteConfirmation) = rememberSaveable { mutableStateOf(false) }
        val (selectedTab, setSelectedTab) = rememberSaveable { mutableStateOf(InstanceDetailsTab.Options) }
        val (editedInstance, setEditedInstance) = rememberSaveable { mutableStateOf(InstanceEditState()) }
        val loaderList = remember { mutableProgressiveStateOf<LoaderError, List<ModLoaderType>>() }
        val loaderVersionList = remember { mutableProgressiveStateOf<LoaderError, List<LoaderVersion>>() }

        val currentInstance = instance.valueOrNull
        val effectiveGameVersion = editedInstance.gameVersion ?: currentInstance?.gameVersion
        val effectiveLoaderType = when (val edit = editedInstance.modLoaderType) {
            is ModLoaderTypeEdit.Loader -> edit.type
            ModLoaderTypeEdit.Vanilla -> null
            null -> currentInstance?.loaderType
        }

        LaunchedEffect(effectiveGameVersion) {
            loaderList.value = ProgressiveOutcome.Incomplete()
            if (effectiveGameVersion != null) {
                loaderList.value = progressive { getAvailableLoaders(effectiveGameVersion) }
            }
        }

        LaunchedEffect(effectiveGameVersion, effectiveLoaderType) {
            loaderVersionList.value = ProgressiveOutcome.Incomplete()
            if (effectiveGameVersion != null && effectiveLoaderType != null) {
                loaderVersionList.value = progressive { getLoaderVersions(effectiveGameVersion, effectiveLoaderType) }
            }
        }

        LaunchedEffect(editedInstance.modLoaderType, editedInstance.modLoaderVersion, loaderVersionList.value) {
            val versions = loaderVersionList.value.valueOrNull
            if (editedInstance.modLoaderType is ModLoaderTypeEdit.Loader &&
                editedInstance.modLoaderVersion == null &&
                versions != null
            ) {
                val recommended = versions.firstOrNull { it.stable } ?: versions.firstOrNull()
                if (recommended != null) {
                    setEditedInstance(editedInstance.copy(modLoaderVersion = recommended.version))
                }
            }
        }

        return InstanceDetailsState(
            instance = instance,
            editedInstance = editedInstance,
            gameVersionList = gameVersionList.valueOrNull ?: emptyList(),
            loaderList = loaderList.value.valueOrNull ?: emptyList(),
            loaderVersionList = loaderVersionList.value.valueOrNull ?: emptyList(),
            promptDeleteConfirmation = promptDeleteConfirmation,
            selectedTab = selectedTab,
            logs = logs,
            installedMods = installedMods
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
            is InstanceDetailsEvent.OnLoaderChange -> setEditedInstance(
                editedInstance.copy(
                    modLoaderType = event.loader?.let(ModLoaderTypeEdit::Loader) ?: ModLoaderTypeEdit.Vanilla,
                    // Reset so the recommended-version effect can pick one that's valid for the new type.
                    modLoaderVersion = null
                )
            )

            is InstanceDetailsEvent.OnLoaderVersionChange ->
                setEditedInstance(editedInstance.copy(modLoaderVersion = event.version))

            is InstanceDetailsEvent.OnModEnabledChange -> globalScope.launch {
                setModEnabled(screen.instanceId, event.fileName, event.enabled)
            }

            InstanceDetailsEvent.CancelEdit -> setEditedInstance(InstanceEditState())
            InstanceDetailsEvent.SaveInstance -> globalScope.launch {
                either {
                    updateInstance(screen.instanceId) {
                        var updatedInstance = it
                        editedInstance.name?.let { name -> updatedInstance = updatedInstance.copy(name = name) }
                        editedInstance.gameVersion?.let { version ->
                            updatedInstance = updatedInstance.copy(gameVersion = version)
                        }
                        if (editedInstance.modLoaderType != null || editedInstance.modLoaderVersion != null) {
                            val type = when (val edit = editedInstance.modLoaderType) {
                                is ModLoaderTypeEdit.Loader -> edit.type
                                ModLoaderTypeEdit.Vanilla -> null
                                null -> updatedInstance.modLoader?.type
                            }
                            updatedInstance = updatedInstance.copy(
                                modLoader = type?.let { loaderType ->
                                    InstanceModLoader(
                                        type = loaderType,
                                        version = editedInstance.modLoaderVersion
                                            ?: updatedInstance.modLoader?.version.orEmpty()
                                    )
                                }
                            )
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
    private fun observeInstalledModsView(): State<List<InstalledModView>?> =
        produceAndCollectAsRetainedState(observeInstalledMods, screen, initial = null) {
            observeInstalledMods(screen.instanceId).map { outcome -> outcome.valueOrNull?.map { it.toView() } }
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
