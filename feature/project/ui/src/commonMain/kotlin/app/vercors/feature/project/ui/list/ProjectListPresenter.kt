package app.vercors.feature.project.ui.list

import androidx.compose.runtime.*
import app.vercors.core.domain.*
import app.vercors.core.ui.state.collectResourceAsStateWithLifecycle
import app.vercors.core.ui.state.mutableResourceStateOf
import app.vercors.core.ui.state.update
import app.vercors.feature.config.gui.domain.usecase.ObserveGuiConfigurationUseCase
import app.vercors.feature.game.domain.usecase.ObserveGameVersionListUseCase
import app.vercors.feature.game.ui.toView
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.domain.usecase.GetPlatformListUseCase
import app.vercors.feature.project.domain.usecase.GetProjectListUseCase
import app.vercors.feature.project.ui.BriefProjectView
import app.vercors.feature.project.ui.details.ProjectDetailsScreen
import app.vercors.feature.project.ui.toView
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.plus
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

@AssistedInject
class ProjectListPresenter(
    @Assisted private val navigator: Navigator,
    private val getPlatformList: GetPlatformListUseCase,
    private val getProjectList: GetProjectListUseCase,
    private val getGameVersionList: ObserveGameVersionListUseCase,
    private val observeGuiConfiguration: ObserveGuiConfigurationUseCase
) : Presenter<ProjectListState, ProjectListEvent> {
    @Composable
    override fun present(): Store<ProjectListState, ProjectListEvent> {
        val presenterScope = rememberCoroutineScope()
        val preferredPlatform by remember(observeGuiConfiguration) {
            observeGuiConfiguration()
                .map { res -> res.map { it.preferredPlatform } }
        }.collectResourceAsStateWithLifecycle()

        val platformList by remember(getPlatformList) {
            ResourceFlowResult { getPlatformList().map { list -> list.map { it.toView() } } }
        }.collectResourceAsStateWithLifecycle()

        val gameVersionList by remember(getGameVersionList) {
            getGameVersionList().map { res -> res.map { it.toView() } }
        }.collectResourceAsStateWithLifecycle()

        var gameVersionSearchQuery by remember { mutableStateOf("") }
        var showSnapshot by remember { mutableStateOf(false) }
        val filter = remember { mutableStateOf(ProjectListFilter()) }
        val projects = remember { mutableResourceStateOf<Page<BriefProjectView>>() }

        LaunchedEffect(platformList) {
            val defaultPlatform =
                platformList.orNull()?.find { it.id == preferredPlatform.orNull() } ?: platformList.orNull()
                    ?.firstOrNull()
            filter.update {
                copy(
                    selectedPlatform = selectedPlatform ?: defaultPlatform,
                    selectedType = selectedType
                        ?: defaultPlatform?.categoryGroups?.keys?.let { types -> ProjectType.entries.first { it in types } }
                )
            }
        }

        LaunchedEffect(filter.value) {
            searchProjects(filter.value, projects)
        }

        return ProjectListState(
            platformList = platformList,
            gameVersionList = gameVersionList,
            gameVersionSearchQuery = gameVersionSearchQuery,
            showSnapshot = showSnapshot,
            filter = filter.value,
            projects = projects.value,
        ) + {
            when (it) {
                is ProjectListEvent.OnProjectPlatformChange -> filter.updateAndResetPage { copy(selectedPlatform = it.platform) }
                is ProjectListEvent.OnProjectTypeChange -> filter.updateAndResetPage { copy(selectedType = it.type) }
                is ProjectListEvent.OnLoaderClick -> filter.updateAndResetPage {
                    copy(selectedLoaders = selectedLoaders.toggle(it.loader))
                }

                is ProjectListEvent.OnSearchQueryChange -> filter.updateAndResetPage { copy(searchQuery = it.query) }
                ProjectListEvent.OnSearchButtonClick -> presenterScope.launch { searchProjects(filter.value, projects) }
                is ProjectListEvent.OnGameVersionSearchQueryChange -> gameVersionSearchQuery = it.query
                is ProjectListEvent.OnGameVersionClick -> filter.updateAndResetPage {
                    copy(selectedGameVersions = selectedGameVersions.toggle(it.version))
                }

                ProjectListEvent.OnShowSnapshotClick -> showSnapshot = !showSnapshot
                is ProjectListEvent.OnCategoryClick -> filter.updateAndResetPage {
                    copy(selectedCategories = selectedCategories.toggle(it.category))
                }

                is ProjectListEvent.OnSortByChange -> filter.updateAndResetPage { copy(sortBy = it.sortBy) }
                is ProjectListEvent.OnPageSizeChange -> filter.updateAndResetPage { copy(pageSize = it.pageSize) }
                is ProjectListEvent.OnOffsetChange -> filter.update { copy(offset = it.offset) }
                ProjectListEvent.OnClearFilters -> filter.update {
                    ProjectListFilter(
                        selectedPlatform = selectedPlatform,
                        selectedType = selectedType
                    )
                }

                is ProjectListEvent.OnProjectClick -> navigator.goTo(ProjectDetailsScreen(it.projectId))
            }
        }
    }

    private suspend fun searchProjects(
        filter: ProjectListFilter,
        projects: MutableState<Resource<Page<BriefProjectView>>>
    ) {
        if (filter.selectedPlatform != null || filter.selectedType != null) {
            ResourceFlowResult {
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
            }.collectLatest { projects.value = it }
        }
    }

    private fun MutableState<ProjectListFilter>.updateAndResetPage(block: ProjectListFilter.() -> ProjectListFilter) =
        update { block().copy(offset = 0) }

    @CircuitInject(ProjectListScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(navigator: Navigator): ProjectListPresenter
    }
}
