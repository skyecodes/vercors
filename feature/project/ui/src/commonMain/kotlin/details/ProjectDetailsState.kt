package app.vercors.feature.project.ui.details

import androidx.compose.runtime.Immutable
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.project.domain.model.*
import app.vercors.feature.project.ui.install.InstallModpackDialog
import app.vercors.feature.project.ui.install.InstallProjectDialog
import app.vercors.feature.project.ui.list.ProjectCategoryView
import app.vercors.feature.project.ui.list.toView
import opensavvy.state.progressive.ProgressiveOutcome

@Immutable
data class ProjectDetailsState(
    val projectId: ProjectId,
    val project: ProgressiveOutcome<ProjectDetailsError, ProjectDetailsView>,
    val promptInstall: Boolean,
    val installProjectDialog: InstallProjectDialog,
    val installModpackDialog: InstallModpackDialog,
)

@Immutable
data class ProjectDetailsView(
    val id: ProjectId,
    val name: String,
    val type: ProjectType,
    val description: String,
    val body: String?,
    val url: String,
    val logoUrl: String?,
    val gameVersions: List<String>,
    val loaders: List<ModLoaderType>,
    val categories: List<ProjectCategoryView>,
    val authors: List<ProjectAuthorView>,
    val links: ProjectLinksView,
)

@Immutable
data class ProjectAuthorView(
    val name: String,
    val url: String?,
    val avatarUrl: String?
)

@Immutable
data class ProjectLinksView(
    val websiteUrl: String?,
    val sourceUrl: String?,
    val issuesUrl: String?,
    val wikiUrl: String?,
    val discordUrl: String?
)

// DataPack needs a different install flow
val ProjectType.isInstallable: Boolean
    get() = this == ProjectType.Mod || this == ProjectType.ResourcePack || this == ProjectType.ShaderPack ||
            this == ProjectType.Modpack || this == ProjectType.World

fun Project.toView(): ProjectDetailsView = ProjectDetailsView(
    id = id,
    name = name,
    type = type,
    description = description,
    body = body,
    url = url,
    logoUrl = logoUrl,
    gameVersions = gameVersions.summarizeStableGameVersions(),
    loaders = loaders,
    categories = categories.map { it.toView() },
    authors = authors.map { ProjectAuthorView(it.name, it.url, it.avatarUrl) },
    links = ProjectLinksView(
        websiteUrl = links.websiteUrl,
        sourceUrl = links.sourceUrl,
        issuesUrl = links.issuesUrl,
        wikiUrl = links.wikiUrl,
        discordUrl = links.discordUrl
    )
)
