package app.vercors.launcher.gui.instance.create

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import app.vercors.launcher.common.domain.instance.model.Instance
import app.vercors.launcher.common.domain.instance.model.InstanceId
import app.vercors.launcher.common.domain.instance.usecase.CreateInstanceUseCase
import app.vercors.launcher.gui.game.GameVersionView
import app.vercors.launcher.gui.game.convert
import app.vercors.launcher.gui.res.*
import app.vercors.shared.domain.Resource
import app.vercors.shared.domain.game.model.GameVersionList
import app.vercors.shared.domain.game.usecase.GetGameVersionListUseCase
import app.vercors.shared.domain.getOrNull
import app.vercors.shared.domain.loader.ModLoaderType
import app.vercors.shared.domain.map
import app.vercors.shared.domain.resourceFlow
import app.vercors.shared.ui.core.configureWithLogger
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
    private val getMinecraftVersionList: GetGameVersionListUseCase,
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
                    resourceFlow(getMinecraftVersionList::invoke).collectLatest {
                        val gameVersionList = it.map(GameVersionList::convert)
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

    val modLoader: ModLoaderType? = null
) : MVIState {
    val filteredGameVersionList: List<GameVersionView> = gameVersionList.getOrNull()?.filter {
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
        title = Res.string.createCustomInstance,
        description = Res.string.createCustomInstanceDescription,
        intent = { CreateInstanceIntent.OnTabClick(Custom) }
    ),
    Modpack(
        icon = Lucide.PackagePlus,
        title = Res.string.importModpack,
        description = Res.string.importModpackDescription,
        intent = { CreateInstanceIntent.OnTabClick(Modpack) }
    ),
    Import(
        icon = Lucide.Import,
        title = Res.string.importOtherLauncher,
        description = Res.string.importOtherLauncherDescription,
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
