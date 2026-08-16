package app.vercors.feature.instance.ui.create

import androidx.compose.runtime.*
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
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceModLoader
import app.vercors.feature.instance.domain.usecase.CreateInstanceUseCase
import app.vercors.feature.instance.ui.message
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.error
import arrow.core.raise.recover
import com.slack.circuit.overlay.OverlayNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import opensavvy.state.ExperimentalProgressiveRaiseApi
import opensavvy.state.arrow.progressive
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.map
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
    private val getAvailableLoaders: GetAvailableLoadersUseCase,
    private val getLoaderVersions: GetLoaderVersionsUseCase,
    private val createInstance: CreateInstanceUseCase,
    private val snackbarService: SnackbarService
) : Presenter<CreateInstanceState, CreateInstanceEvent> {
    @OptIn(ExperimentalProgressiveRaiseApi::class)
    @Composable
    override fun present(): Store<CreateInstanceState, CreateInstanceEvent> {
        val gameVersionList by observeGameVersionListView()
        var instanceName by remember { mutableStateOf("") }
        var selectedGameVersion by remember { mutableStateOf<GameVersionView?>(null) }
        var selectedLoader by remember { mutableStateOf<ModLoaderType?>(null) }
        var selectedLoaderVersion by remember { mutableStateOf<LoaderVersion?>(null) }
        val loaderList = remember { mutableProgressiveStateOf<LoaderError, List<ModLoaderType>>() }
        val loaderVersionList = remember { mutableProgressiveStateOf<LoaderError, List<LoaderVersion>>() }

        LaunchedEffect(selectedGameVersion?.id) {
            val gameVersion = selectedGameVersion
            loaderList.value = ProgressiveOutcome.Incomplete()
            if (gameVersion != null) {
                loaderList.value = progressive { getAvailableLoaders(gameVersion.id) }
            }
        }

        LaunchedEffect(selectedGameVersion?.id, selectedLoader) {
            val gameVersion = selectedGameVersion
            val loader = selectedLoader
            loaderVersionList.value = ProgressiveOutcome.Incomplete()
            if (gameVersion != null && loader != null) {
                val outcome = progressive { getLoaderVersions(gameVersion.id, loader) }
                loaderVersionList.value = outcome
                val versions = outcome.valueOrNull
                if (versions != null) {
                    selectedLoaderVersion = versions.firstOrNull { it.stable } ?: versions.firstOrNull()
                }
            }
        }

        return CreateInstanceState(
            instanceName = instanceName,
            selectedGameVersion = selectedGameVersion,
            gameVersionList = gameVersionList,
            selectedLoader = selectedLoader,
            loaderList = loaderList.value,
            selectedLoaderVersion = selectedLoaderVersion,
            loaderVersionList = loaderVersionList.value
        ) + {
            when (it) {
                is CreateInstanceEvent.OnInstanceNameChange -> instanceName = it.name
                is CreateInstanceEvent.OnGameVersionChange -> {
                    selectedGameVersion = it.version
                    selectedLoader = null
                    selectedLoaderVersion = null
                }

                is CreateInstanceEvent.OnLoaderChange -> {
                    selectedLoader = it.loader
                    selectedLoaderVersion = null
                }

                is CreateInstanceEvent.OnLoaderVersionChange -> selectedLoaderVersion = it.version
                CreateInstanceEvent.OnCancelClick -> navigator.finish(CreateInstanceResult.Cancelled)
                CreateInstanceEvent.OnCreateInstanceClick ->
                    createInstance(instanceName, selectedGameVersion, selectedLoader, selectedLoaderVersion)
            }
        }
    }

    @Composable
    private fun observeGameVersionListView(): State<ProgressiveOutcome<GameVersionError.InvalidManifest, List<GameVersionView>>> =
        produceAndCollectAsProgressiveState {
            observeGameVersionList().map { res -> res.map { it.toView() } }
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
        globalScope.launch {
            recover({ createInstance(instance) }) { error -> snackbarService.error { error.message } }
        }.invokeOnCompletion { error ->
            if (error == null) navigator.finish(CreateInstanceResult.Created(instance.id))
        }
    }
}
