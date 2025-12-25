package app.vercors.launcher.gui.project.list

import androidx.compose.runtime.Immutable
import app.vercors.launcher.gui.config.usecase.ObserveGuiConfigurationUseCase
import app.vercors.launcher.gui.project.BriefProjectView
import app.vercors.launcher.gui.project.list.ProjectListAction.NavigateToProjectDetails
import app.vercors.launcher.gui.project.toView
import app.vercors.shared.domain.*
import app.vercors.shared.domain.game.usecase.GetGameVersionListUseCase
import app.vercors.shared.domain.project.model.ProjectCategory
import app.vercors.shared.domain.project.model.ProjectId
import app.vercors.shared.domain.project.model.ProjectType
import app.vercors.shared.domain.project.usecase.GetPlatformListUseCase
import app.vercors.shared.domain.project.usecase.GetProjectListUseCase
import app.vercors.shared.ui.configureWithLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
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
                is ProjectListIntent.OnSearchQueryChange -> updateState { copy(searchQuery = intent.query) }
                is ProjectListIntent.OnCategoryClick -> updateState { copy(selectedCategories = if (intent.category in selectedCategories) selectedCategories - intent.category else selectedCategories + intent.category) }
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
                                iconData = "",
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
        }

        install {
            onState { old, new ->
                new.let {
                    if (old.selectedPlatform != it.selectedPlatform && it.selectedPlatform != null && it.selectedType !in it.selectedPlatform.categoryGroups.keys)
                        it.copy(selectedType = ProjectType.entries.first { type -> type in it.selectedPlatform.categoryGroups.keys })
                    else it
                }.let {
                    if (old.selectedType != it.selectedType) it.copy(selectedCategories = emptyList()) else new
                }.also {
                    if (it.selectedPlatform != null && it.selectedType != null && (
                                old.selectedPlatform?.id != it.selectedPlatform.id ||
                                        old.selectedType != it.selectedType ||
                                        old.selectedCategories != it.selectedCategories ||
                                        old.searchQuery != it.searchQuery)
                    ) {
                        launch { searchProjects() }
                    }
                }
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
                        query = searchQuery.ifBlank { null },
                        categories = selectedCategories.map { it.id },
                        pageSize = 20
                    ).map { list -> list.map { it.toView() } }
                }.collectLatest {
                    updateState { copy(projects = it) }
                    delay(50)
                    action(ProjectListAction.ScrollUp)
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
    val selectedCategories: List<ProjectCategoryView> = emptyList(),
    val searchQuery: String = "",
    val projects: Resource<List<BriefProjectView>> = Resource.Unloaded
) : MVIState

data class ProjectPlatformView(
    val id: String,
    val name: String,
    val iconData: String,
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
    data class OnSearchQueryChange(val query: String) : ProjectListIntent
    data class OnCategoryClick(val category: ProjectCategoryView) : ProjectListIntent
    data class OnProjectClick(val projectId: ProjectId) : ProjectListIntent
    data object OnSearchButtonClick : ProjectListIntent
}

sealed interface ProjectListAction : MVIAction {
    data class NavigateToProjectDetails(val projectId: ProjectId) : ProjectListAction
    data object ScrollUp : ProjectListAction
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
