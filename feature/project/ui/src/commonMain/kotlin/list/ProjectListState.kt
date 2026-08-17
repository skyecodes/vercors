package app.vercors.feature.project.ui.list

import androidx.compose.runtime.Immutable
import app.vercors.core.domain.Page
import app.vercors.feature.game.domain.model.GameVersionError
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.ui.GameVersionView
import app.vercors.feature.project.domain.model.*
import app.vercors.feature.project.ui.BriefProjectView
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.valueOrNull

@Immutable
data class ProjectListState(
    val platformList: ProgressiveOutcome<PlatformError, List<ProjectPlatformView>>,
    val gameVersionList: ProgressiveOutcome<GameVersionError.InvalidManifest, List<GameVersionView>>,
    val gameVersionSearchQuery: String,
    val showSnapshot: Boolean,
    val filter: ProjectListFilter,
    val projects: ProgressiveOutcome<ProjectSearchError, Page<BriefProjectView>>
) {
    val filteredGameVersionList: List<GameVersionView> = gameVersionList.valueOrNull?.filter {
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

fun ProjectPlatform.toView(): ProjectPlatformView =
    ProjectPlatformView(
        id = id,
        name = name,
        categoryGroups = categories.toView()
    )

fun Map<ProjectType, Map<String, List<ProjectCategory>>>.toView(): Map<ProjectType, List<ProjectCategoryGroupView>> =
    mapValues { (_, groups) ->
        groups.map { (group, categories) ->
            ProjectCategoryGroupView(
                name = group,
                categories = categories.map { it.toView() }
            )
        }.sortedBy { it.name }
    }

fun ProjectCategory.toView(): ProjectCategoryView = ProjectCategoryView(
    id = id,
    slug = slug,
    name = name,
    iconView = icon?.let {
        when (it) {
            is ProjectCategory.Icon.Named -> IconView.Named(it.name)
            is ProjectCategory.Icon.Data -> IconView.Data(it.svgString)
        }
    }
)
