package app.vercors.feature.home.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.vercors.core.ui.animation.appAnimateContentSize
import app.vercors.core.ui.animation.appAnimateDpAsState
import app.vercors.core.ui.animation.shouldAnimate
import app.vercors.core.ui.component.*
import app.vercors.core.ui.layout.AppError
import app.vercors.core.ui.layout.AppSection
import app.vercors.core.ui.modifier.appCard
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.onPrimary
import app.vercors.core.ui.theme.primary
import app.vercors.core.ui.theme.surface
import app.vercors.feature.home.domain.model.HomeSectionType
import app.vercors.feature.instance.ui.*
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.ui.*
import app.vercors.feature.project.ui.details.isInstallable
import app.vercors.feature.project.ui.install.ProjectInstallButton
import app.vercors.feature.project.ui.install.ProjectInstallTarget
import app.vercors.feature.project.ui.list.icon
import app.vercors.shared.ui.*
import app.vercors.shared.ui.overlay.ConfirmDialog
import app.vercors.shared.ui.overlay.ConfirmDialogButton
import app.vercors.shared.ui.overlay.ConfirmDialogButtonStyle
import coil3.compose.rememberAsyncImagePainter
import com.composables.icons.lucide.*
import com.composeunstyled.theme.Theme
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.overlay.OverlayEffect
import com.slack.circuit.sharedelements.SharedElementTransitionScope
import dev.zacsweers.metro.AppScope
import kotlinx.coroutines.launch
import opensavvy.state.progressive.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.stringResource

@CircuitInject(HomeScreen::class, AppScope::class)
@Composable
fun Home(
    store: Store<HomeState, HomeEvent>,
    modifier: Modifier = Modifier,
) {
    val (state, eventSink) = store
    with(eventSink) {
        if (state.promptCreateInstance) {
            OverlayEffect {
                val result = show(state.createInstanceDialog)
                eventSink(HomeEvent.OnCreateInstanceResult(result))
            }
        }

        state.installTarget?.let { target ->
            OverlayEffect {
                when (target) {
                    is ProjectInstallTarget.Project -> eventSink(HomeEvent.OnInstallResult(show(target.dialog)))
                    is ProjectInstallTarget.Modpack -> eventSink(HomeEvent.OnInstallModpackResult(show(target.dialog)))
                }
            }
        }

        state.copyInstanceDialog?.let { dialog ->
            OverlayEffect(dialog) {
                eventSink(HomeEvent.OnCopyInstanceResult(show(dialog)))
            }
        }

        state.pendingDeleteInstanceId?.let {
            OverlayEffect(it) {
                val result = show(
                    ConfirmDialog(
                        messageResource = InstanceRes.string.deleteInstanceConfirmation,
                        actionButton = ConfirmDialogButton(
                            SharedRes.string.delete,
                            Lucide.Trash,
                            ConfirmDialogButtonStyle.Destructive
                        ),
                    )
                )
                eventSink(HomeEvent.OnConfirmDeleteInstance(result))
            }
        }

        Column {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
            ) {
                state.sections.forEach { section ->
                    item(key = section.type) {
                        HomeSection(section)
                    }
                }
            }
        }
    }
}

@Composable
context(_: EventSink<HomeEvent>)
private fun <T> HomeSection(
    section: HomeState.Section<T>
) {
    AppSection(
        icon = section.type.icon,
        text = stringResource(section.type.string),
        modifier = Modifier.padding(vertical = 8.dp),
        titleModifier = Modifier.padding(horizontal = 8.dp),
    ) {
        if (section.type == HomeSectionType.RecentlyPlayed && section.data.valueOrNull.isNullOrEmpty()) {
            NoInstanceFoundDisplay()
        } else {
            HomeSectionList(section)
        }
    }
}

@Composable
context(_: EventSink<HomeEvent>)
private fun <T> HomeSectionList(
    section: HomeState.Section<T>
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val shouldAnimate = shouldAnimate()
    val onScrollButtonClick: (Float) -> Unit = {
        coroutineScope.launch { if (shouldAnimate) listState.animateScrollBy(it) else listState.scrollBy(it) }
    }
    section.data.onFailure {
        AppError(it.message)
    }
    if (section.data !is ProgressiveOutcome.Failure) {
        Box {
            LazyRow(
                state = listState,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                section.data.onSuccess {
                    it.forEachIndexed { index, element ->
                        homeCardItem(
                            data = it,
                            index = index,
                            element = element
                        )
                    }
                }

                section.data.onLoading {
                    repeat(10) { index ->
                        fakeCardItem(
                            index = index,
                            maxIndex = 9,
                            sectionType = section.type
                        )
                    }
                }
            }

            if (listState.canScrollForward) {
                HomeScrollButton(
                    onClick = onScrollButtonClick,
                    isRight = true
                )
            }
            if (listState.canScrollBackward) {
                HomeScrollButton(
                    onClick = onScrollButtonClick,
                    isRight = false
                )
            }
        }
    }
}

context(eventSink: EventSink<HomeEvent>)
private fun <T> LazyListScope.homeCardItem(
    data: List<T>,
    index: Int,
    element: T,
) {
    val modifier = Modifier.padding(
        start = if (index == 0) 8.dp else 0.dp,
        end = if (index == data.lastIndex) 8.dp else 0.dp
    )

    when (element) {
        is BriefInstanceView -> item(element.id) {
            BriefInstanceCard(
                instance = element,
                onInstanceClick = { eventSink(HomeEvent.OnInstanceClick(it)) },
                onInstanceLaunchClick = { eventSink(HomeEvent.OnInstanceLaunchClick(it)) },
                onInstanceStopClick = { eventSink(HomeEvent.OnInstanceStopClick(it)) },
                onInstanceKillClick = { eventSink(HomeEvent.OnInstanceKillClick(it)) },
                onOpenInstanceFolderClick = { eventSink(HomeEvent.OnOpenInstanceFolderClick(it)) },
                onOpenProjectClick = { instanceId, platformId, projectId ->
                    eventSink(HomeEvent.OnOpenProjectFromInstanceClick(instanceId, platformId, projectId))
                },
                onCopyClick = { instanceId, name -> eventSink(HomeEvent.OnCopyInstanceClick(instanceId, name)) },
                onDeleteClick = { eventSink(HomeEvent.OnDeleteInstanceClick(it)) },
                modifier = modifier.size(width = 150.dp, height = 150.dp),
                origin = InstanceSharedElementOrigin.Home
            )
        }

        is BriefProjectView -> item(element.id) {
            HomeProjectCard(
                project = element,
                onProjectClick = { eventSink(HomeEvent.OnProjectClick(it)) },
                onInstallClick = { eventSink(HomeEvent.OnInstallProjectClick(it)) },
                modifier = modifier
            )
        }
    }
}

private fun LazyListScope.fakeCardItem(
    index: Int,
    maxIndex: Int,
    sectionType: HomeSectionType
) {
    val modifier = Modifier.padding(
        start = if (index == 0) 8.dp else 0.dp,
        end = if (index == maxIndex) 8.dp else 0.dp
    )

    item(sectionType to index) {
        when (sectionType) {
            HomeSectionType.RecentlyPlayed -> BriefInstanceCard(
                instance = null,
                onInstanceClick = {},
                onInstanceLaunchClick = {},
                onInstanceStopClick = {},
                onInstanceKillClick = {},
                onOpenInstanceFolderClick = {},
                onOpenProjectClick = { _, _, _ -> },
                onCopyClick = { _, _ -> },
                onDeleteClick = {},
                modifier = modifier.size(width = 150.dp, height = 150.dp),
                origin = InstanceSharedElementOrigin.Home
            )

            else -> HomeProjectCard(
                project = null,
                onProjectClick = {},
                onInstallClick = {},
                modifier = modifier
            )
        }
    }
}

private const val scrollValue = 1000f

@Composable
private fun BoxScope.HomeScrollButton(
    onClick: (Float) -> Unit,
    isRight: Boolean
) {
    AppButton(
        onClick = { onClick(if (isRight) scrollValue else -scrollValue) },
        shape = CircleShape,
        backgroundColor = Theme[colors][surface],
        modifier = Modifier.align(if (isRight) Alignment.CenterEnd else Alignment.CenterStart).padding(8.dp).size(32.dp)
    ) {
        AppIcon(
            imageVector = if (isRight) Lucide.ArrowRight else Lucide.ArrowLeft,
            contentDescription = if (isRight) stringResource(SharedRes.string.next) else stringResource(SharedRes.string.back),
        )
    }
}

@Composable
context(eventSink: EventSink<HomeEvent>)
private fun NoInstanceFoundDisplay() {
    Column(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppText(
            text = stringResource(InstanceRes.string.noInstanceFound)
        )

        AppIconTextButton(
            onClick = { eventSink(HomeEvent.OnCreateInstanceClick) },
            text = stringResource(SharedRes.string.create),
            imageVector = Lucide.Plus,
            backgroundColor = Theme[colors][primary],
            contentColor = Theme[colors][onPrimary]
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun HomeProjectCard(
    project: BriefProjectView?,
    onProjectClick: (ProjectId) -> Unit,
    onInstallClick: (BriefProjectView) -> Unit,
    modifier: Modifier
) = SharedElementTransitionScope {
    if (project != null) {
        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()

        HomeProjectCardLayout(
            modifier = modifier
                .hoverable(interactionSource = interactionSource)
                .projectSharedElement(project.id, "card", ProjectSharedElementOrigin.Home),
            interactionSource = interactionSource,
            onClick = { onProjectClick(project.id) },
            contextMenuItems = projectContextMenuItems(
                project = project,
                onInstallClick = onInstallClick,
                onProjectClick = onProjectClick,
            ),
            icon = {
                Image(
                    painter = rememberAsyncImagePainter(project.logoUrl),
                    contentDescription = project.name,
                    modifier = it
                        .projectSharedElement(project.id, "logo", ProjectSharedElementOrigin.Home)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
            },
            title = {
                AppText(
                    text = project.name,
                    fontWeight = FontWeight.SemiBold,
                    singleLine = true,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .projectSharedElement(project.id, "title", ProjectSharedElementOrigin.Home)
                )
            },
            author = {
                AppText(
                    text = "by ${project.author}",
                    singleLine = true,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            image = {
                HomeProjectBackgroundImage(project, isHovered)
                HomeProjectDescriptionOverlay(project, isHovered, onInstallClick)
                HomeProjectStatsRow(project)
            }
        )
    } else {
        HomeProjectCardLayout(
            modifier = modifier,
            icon = { AppLoadingPlaceholder(it) },
            title = { AppLoadingTextPlaceholder(Modifier.width(150.dp)) },
            author = { AppLoadingTextPlaceholder(Modifier.width(100.dp)) },
            image = { AppLoadingPlaceholder(Modifier.fillMaxSize()) },
        )
    }
}

@Composable
private fun projectContextMenuItems(
    project: BriefProjectView,
    onInstallClick: (BriefProjectView) -> Unit,
    onProjectClick: (ProjectId) -> Unit,
): () -> List<ContextMenuItem> {
    val uriHandler = LocalUriHandler.current
    val installLabel = stringResource(ProjectRes.string.install)
    val openDetailsLabel = stringResource(SharedRes.string.openDetails)
    val openInBrowserLabel = stringResource(SharedRes.string.openInBrowser)
    val primaryColor = Theme[colors][primary]

    return {
        listOfNotNull(
            if (project.type.isInstallable) {
                AppContextMenuItem(
                    label = installLabel,
                    imageVector = Lucide.Download,
                    contentColor = primaryColor,
                    onClick = { onInstallClick(project) },
                )
            } else null,
            AppContextMenuItem(
                label = openDetailsLabel,
                imageVector = Lucide.Info,
                onClick = { onProjectClick(project.id) },
            ),
            AppContextMenuItem(
                label = openInBrowserLabel,
                imageVector = Lucide.ExternalLink,
                onClick = { uriHandler.openUri(project.url) },
            ),
        )
    }
}

@Composable
private fun HomeProjectBackgroundImage(project: BriefProjectView, isHovered: Boolean) {
    val blurRadius by appAnimateDpAsState(if (isHovered) 4.dp else 0.dp)
    val projectImageModifier = Modifier.fillMaxSize().blur(blurRadius)

    project.imageUrl?.let {
        Image(
            painter = rememberAsyncImagePainter(it),
            contentDescription = project.name,
            modifier = projectImageModifier,
            contentScale = ContentScale.Crop
        )
    } ?: Image(
        bitmap = imageResource(ProjectRes.drawable.pack),
        contentDescription = project.name,
        modifier = projectImageModifier,
        contentScale = ContentScale.Crop
    )
}

@Composable
context(_: SharedElementTransitionScope)
private fun HomeProjectDescriptionOverlay(
    project: BriefProjectView,
    isHovered: Boolean,
    onInstallClick: (BriefProjectView) -> Unit,
) {
    val gradientBackground = arrayOf(
        0.0f to Theme[colors][surface],
        1f to Theme[colors][surface].copy(alpha = 0.7f)
    )

    Column(
        modifier = Modifier.background(Brush.verticalGradient(colorStops = gradientBackground))
            .appAnimateContentSize()
                then if (isHovered) Modifier.fillMaxSize() else Modifier.fillMaxWidth().height(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppText(
            text = project.description,
            fontSize = 12.sp,
            modifier = Modifier.padding(8.dp)
                .projectSharedElement(project.id, "description", ProjectSharedElementOrigin.Home),
            minLines = 3,
            maxLines = 3
        )
        if (isHovered) {
            ProjectInstallButton(
                onClick = { onInstallClick(project) },
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
private fun BoxScope.HomeProjectStatsRow(project: BriefProjectView) {
    Row(
        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HomeProjectChip(
            icon = Lucide.Download,
            text = project.downloadsReadable
        )
        HomeProjectChip(
            icon = Lucide.History,
            text = project.lastUpdatedReadable
        )
    }
}

@Composable
context(_: SharedElementTransitionScope)
private fun HomeProjectCardLayout(
    modifier: Modifier,
    interactionSource: MutableInteractionSource? = null,
    onClick: (() -> Unit)? = null,
    contextMenuItems: () -> List<ContextMenuItem> = { emptyList() },
    icon: @Composable context(SharedElementTransitionScope) RowScope.(Modifier) -> Unit,
    title: @Composable context(SharedElementTransitionScope) ColumnScope.() -> Unit,
    author: @Composable context(SharedElementTransitionScope) ColumnScope.() -> Unit,
    image: @Composable context(SharedElementTransitionScope) BoxScope.() -> Unit,
) {
    ContextMenuArea(items = contextMenuItems) {
        Column(
            modifier = modifier
                .appCard(interactionSource = interactionSource, onClick = onClick)
                .size(width = 250.dp, height = 180.dp)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                icon(Modifier.size(36.dp))
                Column {
                    title()
                    author()
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                image()
            }
        }
    }
}

@Composable
private fun HomeProjectChip(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.background(Theme[colors][surface], RoundedCornerShape(4.dp)).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(12.dp)
        )
        AppText(
            text = text,
            fontSize = 10.sp,
        )
    }
}

private val HomeSectionType.string: StringResource
    get() = when (this) {
        HomeSectionType.RecentlyPlayed -> HomeRes.string.recentlyPlayed
        HomeSectionType.FeaturedMods -> HomeRes.string.featuredMods
        HomeSectionType.FeaturedModpacks -> HomeRes.string.featuredModpacks
        HomeSectionType.FeaturedResourcePacks -> HomeRes.string.featuredResourcePacks
        HomeSectionType.FeaturedShaderPacks -> HomeRes.string.featuredShaderPacks
    }

private val HomeSectionType.icon: ImageVector
    get() = when (this) {
        HomeSectionType.RecentlyPlayed -> Lucide.History
        HomeSectionType.FeaturedMods -> ProjectType.Mod.icon
        HomeSectionType.FeaturedModpacks -> ProjectType.Modpack.icon
        HomeSectionType.FeaturedResourcePacks -> ProjectType.ResourcePack.icon
        HomeSectionType.FeaturedShaderPacks -> ProjectType.ShaderPack.icon
    }
