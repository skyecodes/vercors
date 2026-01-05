package app.vercors.feature.project.ui.list

import androidx.compose.runtime.Immutable
import app.vercors.core.domain.Page
import app.vercors.core.domain.Resource
import app.vercors.core.domain.orNull
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.ui.GameVersionView
import app.vercors.feature.project.domain.model.ProjectCategory
import app.vercors.feature.project.domain.model.ProjectPlatform
import app.vercors.feature.project.domain.model.ProjectSortBy
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.ui.BriefProjectView

@Immutable
data class ProjectListState(
    val platformList: Resource<List<ProjectPlatformView>> = Resource.Unloaded,
    val gameVersionList: Resource<List<GameVersionView>> = Resource.Unloaded,
    val gameVersionSearchQuery: String = "",
    val showSnapshot: Boolean = false,
    val filter: ProjectListFilter = ProjectListFilter(),
    val projects: Resource<Page<BriefProjectView>> = Resource.Unloaded
) {
    val filteredGameVersionList: List<GameVersionView> = gameVersionList.orNull()?.filter {
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
