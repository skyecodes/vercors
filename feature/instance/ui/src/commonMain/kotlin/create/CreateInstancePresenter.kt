package app.vercors.feature.instance.ui.create

import androidx.compose.runtime.*
import app.vercors.feature.game.domain.model.LoaderVersion
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.domain.usecase.ObserveAvailableLoadersUseCase
import app.vercors.feature.game.domain.usecase.ObserveGameVersionListUseCase
import app.vercors.feature.game.domain.usecase.ObserveLoaderVersionsUseCase
import app.vercors.feature.game.ui.GameVersionView
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceModLoader
import app.vercors.feature.instance.domain.usecase.CreateInstanceUseCase
import app.vercors.feature.instance.ui.message
import app.vercors.feature.instance.ui.observeAvailableLoadersView
import app.vercors.feature.instance.ui.observeGameVersionListView
import app.vercors.feature.instance.ui.observeLoaderVersionsView
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.error
import arrow.core.raise.recover
import com.slack.circuit.overlay.OverlayNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import opensavvy.state.progressive.valueOrNull

/**
 * Not `@CircuitInject`-wired since [CreateInstanceDialog] isn't a navigated [Screen][app.vercors.shared.ui.Screen] —
 * it's constructed manually by the dialog for each show, mirroring how a `Presenter.Factory` builds a presenter
 * per screen visit, just without Circuit's own screen-resolution machinery.
 */
class CreateInstancePresenter(
    private val navigator: OverlayNavigator<CreateInstanceResult>,
    private val globalScope: CoroutineScope,
    private val observeGameVersionList: ObserveGameVersionListUseCase,
    private val observeAvailableLoaders: ObserveAvailableLoadersUseCase,
    private val observeLoaderVersions: ObserveLoaderVersionsUseCase,
    private val createInstance: CreateInstanceUseCase,
    private val snackbarService: SnackbarService
) : Presenter<CreateInstanceState, CreateInstanceEvent> {
    @Composable
    override fun present(): Store<CreateInstanceState, CreateInstanceEvent> {
        val gameVersionList by observeGameVersionListView(observeGameVersionList)
        val (selectedInstanceType, setSelectedInstanceType) = remember { mutableStateOf(InstanceType.Custom) }
        val (instanceName, setInstanceName) = remember { mutableStateOf("") }
        val (selectedGameVersion, setSelectedGameVersion) = remember { mutableStateOf<GameVersionView?>(null) }
        val (selectedLoader, setSelectedLoader) = remember { mutableStateOf<ModLoaderType?>(null) }
        val (selectedLoaderVersion, setSelectedLoaderVersion) = remember { mutableStateOf<LoaderVersion?>(null) }
        val loaderList by observeAvailableLoadersView(observeAvailableLoaders, selectedGameVersion?.id)
        val loaderVersionList by observeLoaderVersionsView(
            observeLoaderVersions,
            selectedGameVersion?.id,
            selectedLoader
        )

        LaunchedEffect(loaderVersionList) {
            if (selectedLoaderVersion == null) {
                setSelectedLoaderVersion(
                    loaderVersionList.valueOrNull?.let { versions ->
                        versions.firstOrNull { it.stable } ?: versions.firstOrNull()
                    }
                )
            }
        }

        return CreateInstanceState(
            instanceName = instanceName,
            selectedGameVersion = selectedGameVersion,
            gameVersionList = gameVersionList,
            selectedLoader = selectedLoader,
            loaderList = loaderList,
            selectedLoaderVersion = selectedLoaderVersion,
            loaderVersionList = loaderVersionList,
            selectedInstanceType = selectedInstanceType,
        ) + { event ->
            handleEvent(
                event = event,
                setSelectedInstanceType = setSelectedInstanceType,
                instanceName = instanceName,
                setInstanceName = setInstanceName,
                selectedGameVersion = selectedGameVersion,
                setSelectedGameVersion = setSelectedGameVersion,
                selectedLoader = selectedLoader,
                setSelectedLoader = setSelectedLoader,
                selectedLoaderVersion = selectedLoaderVersion,
                setSelectedLoaderVersion = setSelectedLoaderVersion,
            )
        }
    }

    private fun handleEvent(
        event: CreateInstanceEvent,
        setSelectedInstanceType: (InstanceType) -> Unit,
        instanceName: String,
        setInstanceName: (String) -> Unit,
        selectedGameVersion: GameVersionView?,
        setSelectedGameVersion: (GameVersionView?) -> Unit,
        selectedLoader: ModLoaderType?,
        setSelectedLoader: (ModLoaderType?) -> Unit,
        selectedLoaderVersion: LoaderVersion?,
        setSelectedLoaderVersion: (LoaderVersion?) -> Unit,
    ) {
        when (event) {
            is CreateInstanceEvent.OnInstanceTypeChange -> setSelectedInstanceType(event.type)
            is CreateInstanceEvent.OnInstanceNameChange -> setInstanceName(event.name)
            is CreateInstanceEvent.OnGameVersionChange -> {
                setSelectedGameVersion(event.version)
                setSelectedLoader(null)
                setSelectedLoaderVersion(null)
            }

            is CreateInstanceEvent.OnLoaderChange -> {
                setSelectedLoader(event.loader)
                setSelectedLoaderVersion(null)
            }

            is CreateInstanceEvent.OnLoaderVersionChange -> setSelectedLoaderVersion(event.version)
            CreateInstanceEvent.OnCancelClick -> navigator.finish(CreateInstanceResult.Cancelled)
            CreateInstanceEvent.OnCreateInstanceClick ->
                createInstance(instanceName, selectedGameVersion, selectedLoader, selectedLoaderVersion)
            CreateInstanceEvent.OnOpenModpacksScreen -> navigator.finish(CreateInstanceResult.SearchModpacks)
        }
    }

    private fun createInstance(
        instanceName: String,
        selectedGameVersion: GameVersionView?,
        selectedLoader: ModLoaderType?,
        selectedLoaderVersion: LoaderVersion?
    ) {
        val instance = Instance(
            name = instanceName,
            gameVersion = selectedGameVersion!!.id,
            modLoader = selectedLoader?.let { InstanceModLoader(type = it, version = selectedLoaderVersion!!.version) }
        )
        var createdInstance = instance
        globalScope.launch {
            recover({ createdInstance = createInstance(instance) }) { error -> snackbarService.error { error.message } }
        }.invokeOnCompletion { error ->
            if (error == null) navigator.finish(CreateInstanceResult.Created(createdInstance.id))
        }
    }
}
