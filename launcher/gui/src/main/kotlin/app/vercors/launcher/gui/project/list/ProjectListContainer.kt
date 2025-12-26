package app.vercors.launcher.gui.project.list

import androidx.compose.runtime.Immutable
import app.vercors.launcher.gui.config.usecase.ObserveGuiConfigurationUseCase
import app.vercors.launcher.gui.game.GameVersionView
import app.vercors.launcher.gui.game.convert
import app.vercors.launcher.gui.project.BriefProjectView
import app.vercors.launcher.gui.project.list.ProjectListAction.NavigateToProjectDetails
import app.vercors.launcher.gui.project.toView
import app.vercors.shared.domain.*
import app.vercors.shared.domain.game.model.GameVersionList
import app.vercors.shared.domain.game.usecase.GetGameVersionListUseCase
import app.vercors.shared.domain.loader.ModLoaderType
import app.vercors.shared.domain.project.model.ProjectCategory
import app.vercors.shared.domain.project.model.ProjectId
import app.vercors.shared.domain.project.model.ProjectType
import app.vercors.shared.domain.project.usecase.GetPlatformListUseCase
import app.vercors.shared.domain.project.usecase.GetProjectListUseCase
import app.vercors.shared.ui.core.configureWithLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.*
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed

private val logger = KotlinLogging.logger {}

class ProjectListContainer(
    private val getPlatformList: GetPlatformListUseCase,
    private val getProjectList: GetProjectListUseCase,
    private val getGameVersionList: GetGameVersionListUseCase,
    private val observeGuiConfigurationUseCase: ObserveGuiConfigurationUseCase
) : Container<ProjectListState, ProjectListIntent, ProjectListAction> {
    override val store = store(initial = ProjectListState()) {
        configureWithLogger(logger)

        reduce { intent ->
            when (intent) {
                is ProjectListIntent.OnProjectProviderChange -> updateState { copy(selectedPlatform = intent.provider) }
                is ProjectListIntent.OnProjectTypeChange -> updateState { copy(selectedType = intent.type) }
                is ProjectListIntent.OnLoaderClick -> updateState { copy(selectedLoaders = selectedLoaders.toggle(intent.loader)) }
                is ProjectListIntent.OnSearchQueryChange -> updateState { copy(searchQuery = intent.query) }
                is ProjectListIntent.OnGameVersionSearchQueryChange -> updateState { copy(gameVersionSearchQuery = intent.query) }
                is ProjectListIntent.OnGameVersionClick -> updateState {
                    copy(
                        selectedGameVersions = selectedGameVersions.toggle(
                            intent.version
                        )
                    )
                }

                ProjectListIntent.OnShowSnapshotClick -> updateState { copy(showSnapshot = !showSnapshot) }
                is ProjectListIntent.OnCategoryClick -> updateState {
                    copy(
                        selectedCategories = selectedCategories.toggle(
                            intent.category
                        )
                    )
                }
                is ProjectListIntent.OnProjectClick -> action(NavigateToProjectDetails(intent.projectId))
                ProjectListIntent.OnSearchButtonClick -> searchProjects()
            }
        }

        whileSubscribed {
            val preferredPlatform = async { observeGuiConfigurationUseCase().awaitValueOrNull()?.preferredPlatform }
            launch {
                resourceFlow { getPlatformList() }.collectLatest { resource ->
                    val platformList = resource.map {
                        it.map { platform ->
                            ProjectPlatformView(
                                id = platform.id,
                                name = platform.name,
                                categoryGroups = platform.categories.convert()
                            )
                        }
                    }
                    updateState {
                        val defaultPlatform = platformList.getOrNull()?.find { it.id == preferredPlatform.await() }
                            ?: platformList.getOrNull()?.firstOrNull()
                        copy(
                            providerList = platformList,
                            selectedPlatform = selectedPlatform ?: defaultPlatform,
                            selectedType = selectedType
                                ?: defaultPlatform?.categoryGroups?.keys?.let { types -> ProjectType.entries.first { it in types } }
                        )
                    }
                }
            }
            launch {
                resourceFlow { getGameVersionList() }.collectLatest {
                    val gameVersionList = it.map(GameVersionList::convert)
                    updateState { copy(gameVersionList = gameVersionList) }
                }
            }
        }

        install {
            onState { old, new ->
                var new = new
                if (old.selectedPlatform != new.selectedPlatform && new.selectedPlatform != null && new.selectedType !in new.selectedPlatform.categoryGroups.keys)
                    new =
                        new.copy(selectedType = ProjectType.entries.first { type -> type in new.selectedPlatform.categoryGroups.keys })
                if (old.selectedType != new.selectedType)
                    new = new.copy(selectedCategories = emptyList())
                if (new.selectedPlatform != null && new.selectedType != null && (
                            old.selectedPlatform?.id != new.selectedPlatform.id ||
                                    old.selectedType != new.selectedType ||
                                    old.selectedLoaders != new.selectedLoaders ||
                                    old.selectedGameVersions != new.selectedGameVersions ||
                                    old.selectedCategories != new.selectedCategories ||
                                    old.searchQuery != new.searchQuery)
                    ) {
                        launch { searchProjects() }
                    }
                new
            }
        }
    }

    suspend fun PipelineContext<ProjectListState, ProjectListIntent, ProjectListAction>.searchProjects() {
        withState {
            if (selectedPlatform != null || selectedType != null) {
                resourceFlow {
                    getProjectList(
                        platformId = selectedPlatform!!.id,
                        type = selectedType!!,
                        loaders = if (selectedType.requiresLoader) selectedLoaders.ifEmpty { null } else null,
                        gameVersions = selectedGameVersions.map { it.id }.ifEmpty { null },
                        query = searchQuery.ifBlank { null },
                        categories = selectedCategories.map { it.id }.ifEmpty { null },
                        pageSize = 20
                    ).map { list -> list.map { it.toView() } }
                }.collectLatest {
                    updateState { copy(projects = it) }
                }
            }
        }
    }
}

@Immutable
data class ProjectListState(
    val providerList: Resource<List<ProjectPlatformView>> = Resource.Unloaded,
    val selectedPlatform: ProjectPlatformView? = null,
    val selectedType: ProjectType? = null,
    val selectedLoaders: List<ModLoaderType> = emptyList(),
    val gameVersionSearchQuery: String = "",
    val gameVersionList: Resource<List<GameVersionView>> = Resource.Unloaded,
    val selectedGameVersions: List<GameVersionView> = emptyList(),
    val showSnapshot: Boolean = false,
    val selectedCategories: List<ProjectCategoryView> = emptyList(),
    val searchQuery: String = "",
    val projects: Resource<List<BriefProjectView>> = Resource.Unloaded
) : MVIState {
    val filteredGameVersionList: List<GameVersionView> = gameVersionList.getOrNull()?.filter {
        (gameVersionSearchQuery.isBlank() || it.id.contains(
            gameVersionSearchQuery,
            ignoreCase = true
        )) && (showSnapshot || !it.isSnapshot)
    } ?: emptyList()
}

data class ProjectPlatformView(
    val id: String,
    val name: String,
    val categoryGroups: Map<ProjectType, List<ProjectCategoryGroupView>>
)

data class ProjectCategoryGroupView(
    val name: String,
    val categories: List<ProjectCategoryView>
)

data class ProjectCategoryView(
    val id: String,
    val slug: String,
    val name: String?,
    val iconView: IconView?
) {
    val slugSnakeCase: String = slug.replace('-', '_')
}

sealed interface IconView {
    data class Named(val name: String) : IconView
    data class Data(val svgString: String) : IconView
}

sealed interface ProjectListIntent : MVIIntent {
    data class OnProjectProviderChange(val provider: ProjectPlatformView) : ProjectListIntent
    data class OnProjectTypeChange(val type: ProjectType) : ProjectListIntent
    data class OnLoaderClick(val loader: ModLoaderType) : ProjectListIntent
    data class OnSearchQueryChange(val query: String) : ProjectListIntent
    data class OnGameVersionSearchQueryChange(val query: String) : ProjectListIntent
    data class OnGameVersionClick(val version: GameVersionView) : ProjectListIntent
    data object OnShowSnapshotClick : ProjectListIntent
    data class OnCategoryClick(val category: ProjectCategoryView) : ProjectListIntent
    data class OnProjectClick(val projectId: ProjectId) : ProjectListIntent

    data object OnSearchButtonClick : ProjectListIntent
}

sealed interface ProjectListAction : MVIAction {
    data class NavigateToProjectDetails(val projectId: ProjectId) : ProjectListAction
}

fun Map<ProjectType, Map<String, List<ProjectCategory>>>.convert(): Map<ProjectType, List<ProjectCategoryGroupView>> =
    mapValues { (_, groups) ->
        groups.map { (group, categories) ->
            ProjectCategoryGroupView(
                name = group,
                categories = categories.map { category ->
                    ProjectCategoryView(
                        id = category.id,
                        slug = category.slug,
                        name = category.name,
                        iconView = category.icon?.let {
                            when (it) {
                                is ProjectCategory.Icon.Named -> IconView.Named(it.name)
                                is ProjectCategory.Icon.Data -> IconView.Data(it.svgString)
                            }
                        }
                    )
                }
            )
        }.sortedBy { it.name }
    }
