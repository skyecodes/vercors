package app.vercors.feature.project.ui.list

import androidx.compose.runtime.*
import app.vercors.core.domain.Page
import app.vercors.core.domain.map
import app.vercors.core.domain.toggle
import app.vercors.core.ui.state.mutableProgressiveStateOf
import app.vercors.core.ui.state.produceAndCollectAsProgressiveState
import app.vercors.core.ui.state.produceProgressiveState
import app.vercors.core.ui.state.update
import app.vercors.feature.config.domain.model.ConfigurationError
import app.vercors.feature.config.domain.usecase.ObserveConfigurationUseCase
import app.vercors.feature.game.domain.model.GameVersionError
import app.vercors.feature.game.domain.usecase.ObserveGameVersionListUseCase
import app.vercors.feature.game.ui.GameVersionView
import app.vercors.feature.game.ui.toView
import app.vercors.feature.project.domain.model.PlatformError
import app.vercors.feature.project.domain.model.ProjectSearchError
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.domain.usecase.GetPlatformListUseCase
import app.vercors.feature.project.domain.usecase.SearchProjectsUseCase
import app.vercors.feature.project.ui.BriefProjectView
import app.vercors.feature.project.ui.details.ProjectDetailsScreen
import app.vercors.feature.project.ui.toView
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.Store
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import opensavvy.state.ExperimentalProgressiveRaiseApi
import opensavvy.state.arrow.progressive
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.map
import opensavvy.state.progressive.valueOrNull

private val logger = KotlinLogging.logger {}

@AssistedInject
class ProjectListPresenter(
    @Assisted private val navigator: Navigator,
    private val getPlatformList: GetPlatformListUseCase,
    private val searchProjects: SearchProjectsUseCase,
    private val getGameVersionList: ObserveGameVersionListUseCase,
    private val observeConfiguration: ObserveConfigurationUseCase
) : Presenter<ProjectListState, ProjectListEvent> {
    @Composable
    override fun present(): Store<ProjectListState, ProjectListEvent> {
        val presenterScope = rememberCoroutineScope()
        val preferredPlatform by observePreferredPlatform()
        val platformList by observePlatformList()
        val gameVersionList by observeGameVersionList()

        val gameVersionSearchQuery = rememberRetained { mutableStateOf("") }
        val showSnapshot = rememberRetained { mutableStateOf(false) }
        val filter = rememberRetained { mutableStateOf(ProjectListFilter()) }
        val projects = rememberRetained { mutableProgressiveStateOf<ProjectSearchError, Page<BriefProjectView>>() }

        LaunchedEffect(platformList, preferredPlatform) {
            val defaultPlatform =
                platformList.valueOrNull?.find { it.id == preferredPlatform.valueOrNull }
                    ?: platformList.valueOrNull?.firstOrNull()
            filter.update {
                copy(
                    selectedPlatform = selectedPlatform ?: defaultPlatform,
                    selectedType = selectedType ?: defaultPlatform?.categoryGroups?.keys?.let { types ->
                        ProjectType.entries.first { it in types }
                    }
                )
            }
        }

        LaunchedEffect(filter.value) {
            searchProjects(filter.value, projects)
        }

        return ProjectListState(
            platformList = platformList,
            gameVersionList = gameVersionList,
            gameVersionSearchQuery = gameVersionSearchQuery.value,
            showSnapshot = showSnapshot.value,
            filter = filter.value,
            projects = projects.value,
        ) + { handleEvent(it, presenterScope, filter, projects, gameVersionSearchQuery, showSnapshot) }
    }

    @Composable
    private fun observePreferredPlatform(): State<ProgressiveOutcome<ConfigurationError, String>> =
        produceAndCollectAsProgressiveState {
            observeConfiguration().map { res -> res.map { it.preferredPlatform } }
        }

    @Composable
    @OptIn(ExperimentalProgressiveRaiseApi::class)
    private fun observePlatformList(): State<ProgressiveOutcome<PlatformError, List<ProjectPlatformView>>> =
        produceProgressiveState {
            value = progressive { getPlatformList().map { it.toView() } }
        }

    @Composable
    private fun observeGameVersionList(): State<ProgressiveOutcome<GameVersionError.InvalidManifest, List<GameVersionView>>> =
        produceAndCollectAsProgressiveState {
            getGameVersionList().map { res -> res.map { it.toView() } }
        }

    private fun handleEvent(
        event: ProjectListEvent,
        presenterScope: CoroutineScope,
        filter: MutableState<ProjectListFilter>,
        projects: MutableState<ProgressiveOutcome<ProjectSearchError, Page<BriefProjectView>>>,
        gameVersionSearchQuery: MutableState<String>,
        showSnapshot: MutableState<Boolean>
    ) {
        when (event) {
            is ProjectListEvent.OnProjectPlatformChange -> filter.updateAndResetPage { copy(selectedPlatform = event.platform) }
            is ProjectListEvent.OnProjectTypeChange -> filter.updateAndResetPage { copy(selectedType = event.type) }
            is ProjectListEvent.OnLoaderClick -> filter.updateAndResetPage {
                copy(selectedLoaders = selectedLoaders.toggle(event.loader))
            }

            is ProjectListEvent.OnSearchQueryChange -> filter.updateAndResetPage { copy(searchQuery = event.query) }
            ProjectListEvent.OnSearchButtonClick -> presenterScope.launch { searchProjects(filter.value, projects) }
            is ProjectListEvent.OnGameVersionSearchQueryChange -> gameVersionSearchQuery.value = event.query
            is ProjectListEvent.OnGameVersionClick -> filter.updateAndResetPage {
                copy(selectedGameVersions = selectedGameVersions.toggle(event.version))
            }

            ProjectListEvent.OnShowSnapshotClick -> showSnapshot.value = !showSnapshot.value
            is ProjectListEvent.OnCategoryClick -> filter.updateAndResetPage {
                copy(selectedCategories = selectedCategories.toggle(event.category))
            }

            is ProjectListEvent.OnSortByChange -> filter.updateAndResetPage { copy(sortBy = event.sortBy) }
            is ProjectListEvent.OnPageSizeChange -> filter.updateAndResetPage { copy(pageSize = event.pageSize) }
            is ProjectListEvent.OnOffsetChange -> filter.update { copy(offset = event.offset) }
            ProjectListEvent.OnClearFilters -> filter.update {
                ProjectListFilter(
                    selectedPlatform = selectedPlatform,
                    selectedType = selectedType
                )
            }

            is ProjectListEvent.OnProjectClick -> navigator.goTo(ProjectDetailsScreen(event.projectId))
        }
    }

    @OptIn(ExperimentalProgressiveRaiseApi::class)
    private suspend fun searchProjects(
        filter: ProjectListFilter,
        projects: MutableState<ProgressiveOutcome<ProjectSearchError, Page<BriefProjectView>>>
    ) {
        if (filter.selectedPlatform != null || filter.selectedType != null) {
            projects.value = ProgressiveOutcome.Incomplete()
            projects.value = progressive {
                searchProjects(
                    platformId = filter.selectedPlatform!!.id,
                    type = filter.selectedType!!,
                    loaders = if (filter.selectedType.requiresLoader) filter.selectedLoaders.ifEmpty { null } else null,
                    gameVersions = filter.selectedGameVersions.map { it.id }.ifEmpty { null },
                    query = filter.searchQuery.ifBlank { null },
                    categories = filter.selectedCategories.map { it.id }.ifEmpty { null },
                    sortBy = filter.sortBy,
                    pageSize = filter.pageSize,
                    offset = filter.offset
                ).map { it.toView() }
            }
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
