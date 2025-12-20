package app.vercors.launcher.gui.project.list

import androidx.compose.runtime.Immutable
import app.vercors.launcher.gui.home.BriefProjectView
import app.vercors.launcher.gui.project.list.ProjectListAction.NavigateToProjectDetails
import app.vercors.shared.domain.Resource
import app.vercors.shared.domain.project.model.ProjectId
import app.vercors.shared.ui.configureWithLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed

private val logger = KotlinLogging.logger {}

class ProjectListContainer(

) : Container<ProjectListState, ProjectListIntent, ProjectListAction> {
    override val store = store(initial = ProjectListState()) {
        configureWithLogger(logger)

        reduce { intent ->
            when (intent) {
                is ProjectListIntent.OnProjectProviderChange -> updateState { copy(selectedProvider = intent.provider) }
                is ProjectListIntent.OnProjectTypeChange -> updateState { copy(selectedType = intent.provider) }
                is ProjectListIntent.OnSearchQueryChange -> updateState { copy(searchQuery = intent.query) }
                is ProjectListIntent.OnProjectClick -> action(NavigateToProjectDetails(intent.projectId))
                ProjectListIntent.OnSearchButtonClick -> {}
            }
        }

        whileSubscribed {

        }
    }
}

@Immutable
data class ProjectListState(
    val providerList: List<ProjectProviderView> = listOf(
        ProjectProviderView("modrinth", "Modrinth", ""),
        ProjectProviderView("curseforge", "CurseForge", ""),
    ),
    val selectedProvider: ProjectProviderView = providerList.first(),
    val typeList: List<ProjectTypeView> = listOf(
        ProjectTypeView("mod", "Mod", "box"),
    ),
    val selectedType: ProjectTypeView = typeList.first(),
    val searchQuery: String = "",
    val projects: Resource<List<BriefProjectView>> = Resource.Unloaded
) : MVIState

data class ProjectProviderView(
    val id: String,
    val name: String,
    val iconData: String
)

data class ProjectTypeView(
    val id: String,
    val name: String,
    val iconId: String
)

sealed interface ProjectListIntent : MVIIntent {
    data class OnProjectProviderChange(val provider: ProjectProviderView) : ProjectListIntent
    data class OnProjectTypeChange(val provider: ProjectTypeView) : ProjectListIntent
    data class OnSearchQueryChange(val query: String) : ProjectListIntent
    data class OnProjectClick(val projectId: ProjectId) : ProjectListIntent
    data object OnSearchButtonClick : ProjectListIntent
}

sealed interface ProjectListAction : MVIAction {
    data class NavigateToProjectDetails(val projectId: ProjectId) : ProjectListAction
}
