package app.vercors.launcher.demo

import app.vercors.core.domain.Page
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.ui.GameVersionView
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.ui.list.*
import app.vercors.launcher.demo.data.GAME_VERSIONS
import app.vercors.launcher.demo.data.MOD_CATEGORIES
import app.vercors.launcher.demo.data.SEARCH_RESULTS
import app.vercors.launcher.demo.data.toView
import app.vercors.shared.ui.Store
import opensavvy.state.progressive.ProgressiveOutcome

private const val SELECTED_GAME_VERSION = "26.3"
private const val SEARCH_TOTAL_HITS = 7304
private const val MODRINTH_CATEGORY_GROUP = "categories"

fun projectListDemoStore(): Store<ProjectListState, ProjectListEvent> {
    val modrinth = ProjectPlatformView(
        id = "modrinth",
        name = "Modrinth",
        categoryGroups = mapOf(
            ProjectType.Mod to listOf(
                ProjectCategoryGroupView(
                    name = MODRINTH_CATEGORY_GROUP,
                    categories = MOD_CATEGORIES.map { (slug, icon) ->
                        ProjectCategoryView(id = slug, slug = slug, name = null, iconView = IconView.Named(icon))
                    },
                ),
            ),
        ),
    )
    val curseForge = ProjectPlatformView(id = "curseforge", name = "CurseForge", categoryGroups = emptyMap())
    val gameVersions = gameVersionViews()
    return Store(
        state = ProjectListState(
            platformList = ProgressiveOutcome.Success(listOf(modrinth, curseForge)),
            gameVersionList = ProgressiveOutcome.Success(gameVersions),
            gameVersionSearchQuery = "",
            showSnapshot = false,
            filter = ProjectListFilter(
                selectedPlatform = modrinth,
                selectedType = ProjectType.Mod,
                selectedLoaders = listOf(ModLoaderType.Fabric),
                selectedGameVersions = gameVersions.filter { it.id == SELECTED_GAME_VERSION },
            ),
            projects = ProgressiveOutcome.Success(Page(SEARCH_RESULTS.map { it.toView() }, SEARCH_TOTAL_HITS)),
            installTarget = null,
        ),
        eventSink = {},
    )
}

private fun gameVersionViews(): List<GameVersionView> {
    val latestRelease = GAME_VERSIONS.first { (_, snapshot) -> !snapshot }.first
    val latestSnapshot = GAME_VERSIONS.first { (_, snapshot) -> snapshot }.first
    return GAME_VERSIONS.map { (id, snapshot) ->
        GameVersionView(
            id = id,
            isLatestRelease = id == latestRelease,
            isLatestSnapshot = id == latestSnapshot,
            isSnapshot = snapshot,
        )
    }
}
