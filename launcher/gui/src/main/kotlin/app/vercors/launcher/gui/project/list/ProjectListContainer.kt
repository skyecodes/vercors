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
import app.vercors.shared.domain.project.model.ProjectSortBy
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
                is ProjectListIntent.OnProjectProviderChange -> updateState {
                    copy(
                        filter = filter.copy(
                            selectedPlatform = intent.provider,
                            offset = 0
                        )
                    )
                }

                is ProjectListIntent.OnProjectTypeChange -> updateState {
                    copy(
                        filter = filter.copy(
                            selectedType = intent.type,
                            offset = 0
                        )
                    )
                }

                is ProjectListIntent.OnLoaderClick -> updateState {
                    copy(
                        filter = filter.copy(
                            selectedLoaders = filter.selectedLoaders.toggle(intent.loader),
                            offset = 0
                        )
                    )
                }

                is ProjectListIntent.OnSearchQueryChange -> updateState {
                    copy(
                        filter = filter.copy(
                            searchQuery = intent.query,
                            offset = 0
                        )
                    )
                }
                is ProjectListIntent.OnGameVersionSearchQueryChange -> updateState { copy(gameVersionSearchQuery = intent.query) }
                is ProjectListIntent.OnGameVersionClick -> updateState {
                    copy(
                        filter = filter.copy(
                            selectedGameVersions = filter.selectedGameVersions.toggle(intent.version),
                            offset = 0
                        )
                    )
                }
                ProjectListIntent.OnShowSnapshotClick -> updateState { copy(showSnapshot = !showSnapshot) }
                is ProjectListIntent.OnCategoryClick -> updateState {
                    copy(
                        filter = filter.copy(
                            selectedCategories = filter.selectedCategories.toggle(intent.category),
                            offset = 0
                        )
                    )
                }

                is ProjectListIntent.OnSortByChange -> updateState {
                    copy(
                        filter = filter.copy(
                            sortBy = intent.sortBy,
                            offset = 0
                        )
                    )
                }

                is ProjectListIntent.OnPageSizeChange -> updateState {
                    copy(
                        filter = filter.copy(
                            pageSize = intent.pageSize,
                            offset = 0
                        )
                    )
                }

                is ProjectListIntent.OnOffsetChange -> updateState { copy(filter = filter.copy(offset = intent.offset)) }
                is ProjectListIntent.OnClearFilters -> updateState {
                    copy(
                        filter = ProjectListFilter(
                            selectedPlatform = filter.selectedPlatform,
                            selectedType = filter.selectedType
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
                    resource.onError { error -> logger.error(error) { "Failed to get platform list" } }
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
                            filter = filter.copy(
                                selectedPlatform = filter.selectedPlatform ?: defaultPlatform,
                                selectedType = filter.selectedType
                                    ?: defaultPlatform?.categoryGroups?.keys?.let { types -> ProjectType.entries.first { it in types } }
                            )
                        )
                    }
                }
            }
            launch {
                resourceFlow { getGameVersionList() }.collectLatest {
                    it.onError { error -> logger.error(error) { "Failed to get game version list" } }
                    val gameVersionList = it.map(GameVersionList::convert)
                    updateState { copy(gameVersionList = gameVersionList) }
                }
            }
        }

        install {
            onState { old, new ->
                var new = new
                if (old.filter.selectedPlatform != new.filter.selectedPlatform && new.filter.selectedPlatform != null && new.filter.selectedType !in new.filter.selectedPlatform.categoryGroups.keys)
                    new = new.copy(filter = new.filter.copy(selectedType = ProjectType.entries.first { type ->
                        type in new.filter.selectedPlatform.categoryGroups.keys
                    }))
                if (old.filter.selectedType != new.filter.selectedType)
                    new = new.copy(filter = new.filter.copy(selectedCategories = emptyList()))
                if (new.filter.selectedPlatform != null && new.filter.selectedType != null && old.filter != new.filter) {
                    launch { searchProjects() }
                }
                new
            }
        }
    }

    suspend fun PipelineContext<ProjectListState, ProjectListIntent, ProjectListAction>.searchProjects() {
        withState {
            if (filter.selectedPlatform != null || filter.selectedType != null) {
                resourceFlow {
                    getProjectList(
                        platformId = filter.selectedPlatform!!.id,
                        type = filter.selectedType!!,
                        loaders = if (filter.selectedType.requiresLoader) filter.selectedLoaders.ifEmpty { null } else null,
                        gameVersions = filter.selectedGameVersions.map { it.id }.ifEmpty { null },
                        query = filter.searchQuery.ifBlank { null },
                        categories = filter.selectedCategories.map { it.id }.ifEmpty { null },
                        sortBy = filter.sortBy,
                        pageSize = filter.pageSize,
                        offset = filter.offset
                    ).map { list -> list.map { it.toView() } }
                }.collectLatest {
                    it.onError { error -> logger.error(error) { "Failed to search projects" } }
                    updateState {
                        when (it) {
                            is Resource.Loaded -> when (val res = it.result) {
                                is Result.Error -> copy(
                                    projects = it.map { page -> page.items },
                                    totalElements = null
                                )

                                is Result.Success -> copy(
                                    projects = it.map { page -> page.items },
                                    totalElements = res.data.total
                                )
                            }

                            else -> copy(projects = it.map { page -> page.items })
                        }
                    }
                }
            }
        }
    }
}

@Immutable
data class ProjectListState(
    val providerList: Resource<List<ProjectPlatformView>> = Resource.Unloaded,
    val gameVersionList: Resource<List<GameVersionView>> = Resource.Unloaded,
    val gameVersionSearchQuery: String = "",
    val showSnapshot: Boolean = false,
    val filter: ProjectListFilter = ProjectListFilter(),
    val projects: Resource<List<BriefProjectView>> = Resource.Unloaded,
    val totalElements: Int? = null,
) : MVIState {
    val filteredGameVersionList: List<GameVersionView> = gameVersionList.getOrNull()?.filter {
        (gameVersionSearchQuery.isBlank() || it.id.contains(
            gameVersionSearchQuery,
            ignoreCase = true
        )) && (showSnapshot || !it.isSnapshot)
    } ?: emptyList()
}

data class ProjectListFilter(
    val selectedPlatform: ProjectPlatformView? = null,
    val selectedType: ProjectType? = null,
    val selectedLoaders: List<ModLoaderType> = emptyList(),
    val selectedGameVersions: List<GameVersionView> = emptyList(),
    val selectedCategories: List<ProjectCategoryView> = emptyList(),
    val searchQuery: String = "",
    val sortBy: ProjectSortBy = ProjectSortBy.Relevance,
    val pageSize: Int = 20,
    val offset: Int = 0,
)

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
    data class OnSortByChange(val sortBy: ProjectSortBy) : ProjectListIntent
    data class OnPageSizeChange(val pageSize: Int) : ProjectListIntent
    data class OnOffsetChange(val offset: Int) : ProjectListIntent
    data object OnClearFilters : ProjectListIntent
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
