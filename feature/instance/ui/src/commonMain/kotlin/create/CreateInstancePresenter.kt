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
import app.vercors.feature.instance.domain.model.InstanceValidationError
import app.vercors.feature.instance.domain.usecase.CreateInstanceUseCase
import app.vercors.feature.instance.ui.details.InstanceDetailsScreen
import app.vercors.feature.instance.ui.message
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.SnackbarService
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.error
import arrow.core.raise.recover
import com.slack.circuit.codegen.annotations.CircuitInject
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
class CreateInstancePresenter(
    @Assisted private val navigator: Navigator,
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
        var selectedTab by remember { mutableStateOf(CreateInstanceTab.Custom) }
        var instanceName by remember { mutableStateOf("") }
        var selectedGameVersion by remember { mutableStateOf<GameVersionView?>(null) }
        var showSnapshot by remember { mutableStateOf(false) }
        var gameVersionFilter by remember { mutableStateOf("") }
        var selectedLoader by remember { mutableStateOf<ModLoaderType?>(null) }
        var selectedLoaderVersion by remember { mutableStateOf<LoaderVersion?>(null) }
        val loaderList = remember { mutableProgressiveStateOf<LoaderError, List<ModLoaderType>>() }
        val loaderVersionList = remember { mutableProgressiveStateOf<LoaderError, List<LoaderVersion>>() }
        val validationErrors = remember { mutableStateListOf<InstanceValidationError>() }

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
            gameVersionList = gameVersionList,
            selectedTab = selectedTab,
            instanceName = instanceName,
            selectedGameVersion = selectedGameVersion,
            showSnapshot = showSnapshot,
            gameVersionFilter = gameVersionFilter,
            selectedLoader = selectedLoader,
            loaderList = loaderList.value,
            selectedLoaderVersion = selectedLoaderVersion,
            loaderVersionList = loaderVersionList.value,
            validationErrors = validationErrors
        ) + {
            when (it) {
                is CreateInstanceEvent.OnTabClick -> selectedTab = it.tab
                is CreateInstanceEvent.OnInstanceNameChange -> instanceName = it.name
                is CreateInstanceEvent.OnGameVersionChange -> {
                    selectedGameVersion = it.version
                    selectedLoader = null
                    selectedLoaderVersion = null
                }

                is CreateInstanceEvent.OnGameVersionSearchChange -> gameVersionFilter = it.search
                CreateInstanceEvent.OnShowSnapshotClick -> showSnapshot = !showSnapshot
                is CreateInstanceEvent.OnLoaderChange -> {
                    selectedLoader = it.loader
                    selectedLoaderVersion = null
                }

                is CreateInstanceEvent.OnLoaderVersionChange -> selectedLoaderVersion = it.version
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
            if (error == null) navigator.goTo(InstanceDetailsScreen(instance.id))
        }
    }

    @CircuitInject(CreateInstanceScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(navigator: Navigator): CreateInstancePresenter
    }
}
