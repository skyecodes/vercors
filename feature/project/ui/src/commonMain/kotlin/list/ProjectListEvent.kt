package app.vercors.feature.project.ui.list

import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.ui.GameVersionView
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.domain.model.ProjectSortBy
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.ui.BriefProjectView
import app.vercors.feature.project.ui.install.InstallModpackResult
import app.vercors.feature.project.ui.install.InstallProjectResult
import app.vercors.shared.ui.Event

sealed interface ProjectListEvent : Event {
    data class OnProjectPlatformChange(val platform: ProjectPlatformView) : ProjectListEvent
    data class OnProjectTypeChange(val type: ProjectType) : ProjectListEvent
    data class OnLoaderClick(val loader: ModLoaderType) : ProjectListEvent
    data class OnSearchQueryChange(val query: String) : ProjectListEvent
    data class OnGameVersionSearchQueryChange(val query: String) : ProjectListEvent
    data class OnGameVersionClick(val version: GameVersionView) : ProjectListEvent
    data object OnShowSnapshotClick : ProjectListEvent
    data class OnCategoryClick(val category: ProjectCategoryView) : ProjectListEvent
    data class OnSortByChange(val sortBy: ProjectSortBy) : ProjectListEvent
    data class OnPageSizeChange(val pageSize: Int) : ProjectListEvent
    data class OnOffsetChange(val offset: Int) : ProjectListEvent
    data object OnClearFilters : ProjectListEvent
    data class OnProjectClick(val projectId: ProjectId) : ProjectListEvent
    data object OnSearchButtonClick : ProjectListEvent
    data class OnInstallProjectClick(val project: BriefProjectView) : ProjectListEvent
    data class OnInstallResult(val result: InstallProjectResult) : ProjectListEvent
    data class OnInstallModpackResult(val result: InstallModpackResult) : ProjectListEvent
}
