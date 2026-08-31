package app.vercors.feature.project.ui.details

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.vercors.core.ui.component.*
import app.vercors.core.ui.layout.AppError
import app.vercors.core.ui.layout.AppLoading
import app.vercors.core.ui.modifier.appCard
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.surface2
import app.vercors.feature.game.ui.*
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.ui.*
import app.vercors.feature.project.ui.install.ProjectInstallButton
import app.vercors.shared.ui.EventSink
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.openInBrowser
import coil3.compose.rememberAsyncImagePainter
import com.composables.icons.fontawesome.FontAwesome
import com.composables.icons.fontawesome.brands.Discord
import com.composables.icons.fontawesome.brands.Github
import com.composables.icons.lucide.*
import com.composeunstyled.theme.Theme
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.overlay.OverlayEffect
import com.slack.circuit.sharedelements.SharedElementTransitionScope
import dev.zacsweers.metro.AppScope
import opensavvy.state.progressive.onFailure
import opensavvy.state.progressive.onIncomplete
import opensavvy.state.progressive.onSuccess
import opensavvy.state.progressive.valueOrNull
import org.jetbrains.compose.resources.stringResource

@CircuitInject(ProjectDetailsScreen::class, AppScope::class)
@Composable
fun ProjectDetails(
    store: Store<ProjectDetailsState, ProjectDetailsEvent>,
    modifier: Modifier = Modifier,
) {
    val (state, eventSink) = store

    if (state.promptInstall) {
        OverlayEffect {
            if (state.project.valueOrNull?.type == ProjectType.Modpack) {
                val result = show(state.installModpackDialog)
                eventSink(ProjectDetailsEvent.OnInstallModpackResult(result))
            } else {
                val result = show(state.installProjectDialog)
                eventSink(ProjectDetailsEvent.OnInstallResult(result))
            }
        }
    }

    with(eventSink) {
        state.project.onSuccess { ProjectDetails(state.projectId, it, modifier) }
        state.project.onFailure { AppError(it.message) }
        state.project.onIncomplete { AppLoading() }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
context(eventSink: EventSink<ProjectDetailsEvent>)
private fun ProjectDetails(
    projectId: ProjectId,
    project: ProjectDetailsView,
    modifier: Modifier
) = SharedElementTransitionScope {
    Column(
        modifier = modifier.padding(8.dp).fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ProjectDetailsHeader(projectId, project)
        Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProjectDescriptionColumn(project, Modifier.weight(1f))
            ProjectInfoSidebar(project, Modifier.width(280.dp))
        }
    }
}

@Composable
context(_: SharedElementTransitionScope, eventSink: EventSink<ProjectDetailsEvent>)
private fun ProjectDetailsHeader(projectId: ProjectId, project: ProjectDetailsView) {
    Row(
        modifier = Modifier.appCard().padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()
            .projectSharedElement(projectId, "card"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(project.logoUrl),
            contentDescription = project.name,
            modifier = Modifier.size(64.dp)
                .projectSharedElement(projectId, "logo")
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AppText(
                text = project.name,
                fontWeight = FontWeight.SemiBold,
                singleLine = true,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.projectSharedElement(projectId, "title")
            )
            AppText(
                text = project.description,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.projectSharedElement(projectId, "description")
            )
        }
        if (project.type.isInstallable) {
            ProjectInstallButton(onClick = { eventSink(ProjectDetailsEvent.OnInstallClick) })
        }

        val uriHandler = LocalUriHandler.current
        AppMenuButton {
            AppMenuItem(
                onClick = { uriHandler.openUri(project.url) },
                text = stringResource(SharedRes.string.openInBrowser),
                imageVector = Lucide.ExternalLink
            )
        }
    }
}

@Composable
private fun ProjectDescriptionColumn(project: ProjectDetailsView, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.appCard().verticalScroll(rememberScrollState())
    ) {
        val body = project.body
        val innerPadding = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        if (body != null) AppMarkdown(
            markdown = body,
            modifier = innerPadding
        ) else AppText(
            text = project.description,
            modifier = innerPadding
        )
    }
}

@Composable
private fun ProjectInfoSidebar(project: ProjectDetailsView, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (project.gameVersions.isNotEmpty()) {
            ProjectInfoSection(stringResource(GameRes.string.gameVersion)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    project.gameVersions.forEach {
                        ProjectInfoChip(text = it)
                    }
                }
            }
        }

        if (project.loaders.isNotEmpty()) {
            ProjectInfoSection(stringResource(GameRes.string.loader)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    project.loaders.forEach {
                        ProjectInfoChip(text = it.displayName, imageVector = it.icon)
                    }
                }
            }
        }

        if (project.categories.isNotEmpty()) {
            ProjectInfoSection(stringResource(ProjectRes.string.categories)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    project.categories.forEach { category ->
                        ProjectInfoChip(
                            text = translateCategory(category),
                            painter = category.iconView?.painter
                        )
                    }
                }
            }
        }

        ProjectLinksSection(project.links)

        if (project.authors.isNotEmpty()) {
            ProjectInfoSection(stringResource(ProjectRes.string.authors)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    project.authors.forEach { author ->
                        ProjectAuthorRow(author)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectLinksSection(links: ProjectLinksView) {
    val hasLinks = listOfNotNull(links.websiteUrl, links.sourceUrl, links.issuesUrl, links.wikiUrl, links.discordUrl)
        .isNotEmpty()
    if (!hasLinks) return

    ProjectInfoSection(stringResource(ProjectRes.string.links)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            links.websiteUrl?.let { ProjectLinkRow(Lucide.Globe, stringResource(ProjectRes.string.website), it) }
            links.sourceUrl?.let {
                ProjectLinkRow(
                    FontAwesome.Brands.Github,
                    stringResource(ProjectRes.string.sourceCode),
                    it
                )
            }
            links.issuesUrl?.let { ProjectLinkRow(Lucide.Bug, stringResource(ProjectRes.string.issues), it) }
            links.wikiUrl?.let { ProjectLinkRow(Lucide.BookOpen, stringResource(ProjectRes.string.wiki), it) }
            links.discordUrl?.let {
                ProjectLinkRow(
                    FontAwesome.Brands.Discord,
                    stringResource(ProjectRes.string.discord),
                    it
                )
            }
        }
    }
}

@Composable
private fun ProjectLinkRow(imageVector: ImageVector, label: String, url: String) {
    val uriHandler = LocalUriHandler.current
    AppIconTextButton(
        onClick = { uriHandler.openUri(url) },
        text = label,
        imageVector = imageVector,
        backgroundColor = Color.Unspecified,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
    )
}

@Composable
private fun ProjectAuthorRow(author: ProjectAuthorView) {
    val url = author.url
    val uriHandler = LocalUriHandler.current
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier.fillMaxWidth()
            .let {
                if (url != null) it.clip(RoundedCornerShape(4.dp))
                    .clickable(
                        interactionSource = interactionSource,
                        onClick = { uriHandler.openUri(url) }
                    ).pointerHoverIcon(PointerIcon.Hand) else it

            }
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(author.avatarUrl),
            contentDescription = author.name,
            modifier = Modifier.size(24.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        AppText(text = author.name, fontSize = 12.sp, singleLine = true, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ProjectInfoSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().appCard().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AppText(text = title, fontWeight = FontWeight.SemiBold)
        content()
    }
}

@Composable
private fun ProjectInfoChip(
    text: String,
    imageVector: ImageVector? = null,
    painter: Painter? = null
) {
    Row(
        modifier = Modifier.background(Theme[colors][surface2], RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            imageVector != null -> AppIcon(
                imageVector = imageVector,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )

            painter != null -> AppIcon(painter = painter, contentDescription = null, modifier = Modifier.size(14.dp))
        }
        AppText(text = text, fontSize = 12.sp)
    }
}
