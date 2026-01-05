package app.vercors.feature.instance.ui.create

import androidx.compose.runtime.*
import app.vercors.core.domain.map
import app.vercors.core.ui.state.collectResourceAsStateWithLifecycle
import app.vercors.feature.game.domain.usecase.ObserveGameVersionListUseCase
import app.vercors.feature.game.ui.GameVersionView
import app.vercors.feature.game.ui.toView
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.usecase.CreateInstanceUseCase
import app.vercors.feature.instance.ui.details.InstanceDetailsScreen
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.plus
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AssistedInject
class CreateInstancePresenter(
    @Assisted private val navigator: Navigator,
    private val globalScope: CoroutineScope,
    private val observeGameVersionList: ObserveGameVersionListUseCase,
    private val createInstance: CreateInstanceUseCase
) : Presenter<CreateInstanceState, CreateInstanceEvent> {
    @Composable
    override fun present(): Store<CreateInstanceState, CreateInstanceEvent> {
        val gameVersionList by observeGameVersionList().collectResourceAsStateWithLifecycle()
        val mappedGameVersionList = remember(gameVersionList) { gameVersionList.map { it.toView() } }
        var selectedTab by remember { mutableStateOf(CreateInstanceTab.Custom) }
        var instanceName by remember { mutableStateOf("") }
        var selectedGameVersion by remember { mutableStateOf<GameVersionView?>(null) }
        var showSnapshot by remember { mutableStateOf(false) }
        var gameVersionFilter by remember { mutableStateOf("") }

        return CreateInstanceState(
            gameVersionList = mappedGameVersionList,
            selectedTab = selectedTab,
            instanceName = instanceName,
            selectedGameVersion = selectedGameVersion,
            showSnapshot = showSnapshot,
            gameVersionFilter = gameVersionFilter
        ) + {
            when (it) {
                is CreateInstanceEvent.OnTabClick -> selectedTab = it.tab
                is CreateInstanceEvent.OnInstanceNameChange -> instanceName = it.name
                is CreateInstanceEvent.OnGameVersionChange -> selectedGameVersion = it.version
                is CreateInstanceEvent.OnGameVersionSearchChange -> gameVersionFilter = it.search
                CreateInstanceEvent.OnShowSnapshotClick -> showSnapshot = !showSnapshot
                CreateInstanceEvent.OnCreateInstanceClick -> {
                    val instance = Instance(
                        name = instanceName,
                        gameVersion = selectedGameVersion!!.id,
                    )
                    globalScope.launch {
                        createInstance(instance)
                    }.invokeOnCompletion { error ->
                        if (error == null) navigator.goTo(InstanceDetailsScreen(instance.id))
                    }
                }
            }
        }
    }

    @CircuitInject(CreateInstanceScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(navigator: Navigator): CreateInstancePresenter
    }
}
