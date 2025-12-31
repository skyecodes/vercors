package app.vercors.feature.instance.ui.create

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import app.vercors.core.domain.Resource
import app.vercors.core.domain.map
import app.vercors.core.domain.orNull
import app.vercors.feature.game.domain.usecase.ObserveGameVersionListUseCase
import app.vercors.feature.game.ui.GameVersionView
import app.vercors.feature.game.ui.toView
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.usecase.CreateInstanceUseCase
import app.vercors.feature.instance.ui.*
import app.vercors.shared.ui.configureWithLogger
import com.composables.icons.lucide.Import
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.PackagePlus
import com.composables.icons.lucide.SquarePlus
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import pro.respawn.flowmvi.api.*
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce

private val logger = KotlinLogging.logger {}

class CreateInstanceContainer(
    private val observeGameVersionList: ObserveGameVersionListUseCase,
    private val createInstance: CreateInstanceUseCase
) : Container<CreateInstanceState, CreateInstanceIntent, CreateInstanceAction> {
    override val store: Store<CreateInstanceState, CreateInstanceIntent, CreateInstanceAction> =
        store(CreateInstanceState()) {
            configureWithLogger(logger)

            reduce { intent ->
                when (intent) {
                    is CreateInstanceIntent.OnTabClick -> updateState { copy(selectedTab = intent.tab) }
                    is CreateInstanceIntent.OnInstanceNameChange -> updateState { copy(instanceName = intent.name) }
                    is CreateInstanceIntent.OnGameVersionChange -> updateState { copy(selectedGameVersion = intent.version) }
                    is CreateInstanceIntent.OnGameVersionSearchChange -> updateState { copy(gameVersionFilter = intent.search) }
                    CreateInstanceIntent.OnShowSnapshotClick -> updateState { copy(showSnapshot = !showSnapshot) }
                    CreateInstanceIntent.OnCreateInstanceClick -> withState {
                        val instance = Instance(
                            name = instanceName,
                            gameVersion = selectedGameVersion!!.id,
                        )
                        createInstance(instance)
                        action(CreateInstanceAction.NavigateToInstanceDetails(instance.id))
                    }
                }
            }

            init {
                launch {
                    observeGameVersionList().collectLatest { res ->
                        val gameVersionList = res.map { it.toView() }
                        updateState { copy(gameVersionList = gameVersionList) }
                    }
                }
            }

            install {
                onState { _, new ->
                    new.copy(
                        selectedGameVersion = new.filteredGameVersionList.find { it == new.selectedGameVersion }
                            ?: new.filteredGameVersionList.firstOrNull(GameVersionView::isLatestRelease)
                            ?: new.filteredGameVersionList.firstOrNull()
                    )
                }
            }
        }
}

@Immutable
data class CreateInstanceState(
    val selectedTab: CreateInstanceTab = CreateInstanceTab.Custom,
    val instanceName: String = "",

    val selectedGameVersion: GameVersionView? = null,
    val gameVersionList: Resource<List<GameVersionView>> = Resource.Unloaded,
    val showSnapshot: Boolean = false,
    val gameVersionFilter: String = "",

    //val modLoader: ModLoaderType? = null
) : MVIState {
    val filteredGameVersionList: List<GameVersionView> = gameVersionList.orNull()?.filter {
        (gameVersionFilter.isBlank() || it.id.contains(
            gameVersionFilter,
            ignoreCase = true
        )) && (showSnapshot || !it.isSnapshot)
    } ?: emptyList()
    val canCreateInstance: Boolean = instanceName.isNotBlank() && selectedGameVersion != null
}

enum class CreateInstanceTab(
    val icon: ImageVector,
    val title: StringResource,
    val description: StringResource,
    val intent: () -> CreateInstanceIntent
) {
    Custom(
        icon = Lucide.SquarePlus,
        title = InstanceRes.string.createCustomInstance,
        description = InstanceRes.string.createCustomInstanceDescription,
        intent = { CreateInstanceIntent.OnTabClick(Custom) }
    ),
    Modpack(
        icon = Lucide.PackagePlus,
        title = InstanceRes.string.importModpack,
        description = InstanceRes.string.importModpackDescription,
        intent = { CreateInstanceIntent.OnTabClick(Modpack) }
    ),
    Import(
        icon = Lucide.Import,
        title = InstanceRes.string.importOtherLauncher,
        description = InstanceRes.string.importOtherLauncherDescription,
        intent = { CreateInstanceIntent.OnTabClick(Import) }
    )
}

sealed interface CreateInstanceIntent : MVIIntent {
    data class OnTabClick(val tab: CreateInstanceTab) : CreateInstanceIntent
    data class OnInstanceNameChange(val name: String) : CreateInstanceIntent
    data class OnGameVersionChange(val version: GameVersionView?) : CreateInstanceIntent
    data class OnGameVersionSearchChange(val search: String) : CreateInstanceIntent
    data object OnShowSnapshotClick : CreateInstanceIntent
    data object OnCreateInstanceClick : CreateInstanceIntent
}

sealed interface CreateInstanceAction : MVIAction {
    data object NavigateToModpackList : CreateInstanceAction
    data class NavigateToInstanceDetails(val id: InstanceId) : CreateInstanceAction
}
