package app.vercors.launcher.gui.project.list

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.vercors.launcher.gui.home.BriefProjectView
import app.vercors.launcher.gui.res.*
import app.vercors.shared.domain.Resource
import app.vercors.shared.domain.Result
import app.vercors.shared.domain.getOrNull
import app.vercors.shared.domain.project.model.ProjectId
import app.vercors.shared.domain.project.model.ProjectType
import app.vercors.shared.ui.animation.appAnimateItem
import app.vercors.shared.ui.animation.appSharedElement
import app.vercors.shared.ui.component.*
import app.vercors.shared.ui.layout.AppError
import app.vercors.shared.ui.modifier.appCard
import app.vercors.shared.ui.theme.colors
import app.vercors.shared.ui.theme.surface2
import coil3.compose.rememberAsyncImagePainter
import com.composables.icons.lucide.*
import com.composeunstyled.theme.Theme
import nl.jacobras.humanreadable.HumanReadable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.decodeToSvgPainter
import org.jetbrains.compose.resources.stringResource
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.dsl.subscribe
import java.util.Locale.getDefault

@Composable
context(
    _: AnimatedContentScope,
    _: SharedTransitionScope
)
fun ProjectListScreen(
    container: ProjectListContainer,
    navigateToProjectDetails: (ProjectId) -> Unit
) = with(container.store) {
    val listState = rememberLazyListState()

    val state by subscribe { action ->
        when (action) {
            is ProjectListAction.NavigateToProjectDetails -> navigateToProjectDetails(action.projectId)
            ProjectListAction.ScrollUp -> listState.scrollToItem(0)
        }
    }

    ProjectListScreen(state, listState)
}

@Composable
context(
    _: IntentReceiver<ProjectListIntent>,
    _: AnimatedContentScope,
    _: SharedTransitionScope
)
private fun ProjectListScreen(state: ProjectListState, listState: LazyListState) {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 8.dp, start = 8.dp, end = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ProjectListHeader(state)
        Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProjectFilters(state)
            ProjectList(state, listState)
        }
    }
}

@Composable
context(store: IntentReceiver<ProjectListIntent>)
private fun ProjectListHeader(state: ProjectListState) {
    Row(
        modifier = Modifier.appCard().padding(8.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AppSelectField(
            value = state.selectedPlatform,
            items = state.providerList.getOrNull() ?: emptyList(),
            onValueChange = { store.intent(ProjectListIntent.OnProjectProviderChange(it!!)) },
            textConverter = { it?.name ?: "" },
            backgroundColor = Theme[colors][surface2],
            modifier = Modifier.width(150.dp)
        )

        AppSelectField(
            value = state.selectedType ?: ProjectType.entries.first(),
            items = ProjectType.entries.filter { state.selectedPlatform?.categoryGroups?.keys?.contains(it) == true },
            onValueChange = { store.intent(ProjectListIntent.OnProjectTypeChange(it)) },
            textConverter = { stringResource(it.displayName) },
            backgroundColor = Theme[colors][surface2],
            modifier = Modifier.width(150.dp)
        )

        AppTextField(
            placeholder = { AppText(stringResource(Res.string.search)) },
            initialValue = state.searchQuery,
            onValueChange = { store.intent(ProjectListIntent.OnSearchQueryChange(it)) },
            backgroundColor = Theme[colors][surface2],
            modifier = Modifier.weight(1f)
        )

        AppIconTextButton(
            onClick = { store.intent(ProjectListIntent.OnSearchButtonClick) },
            imageVector = Lucide.Search,
            text = stringResource(Res.string.search),
            backgroundColor = Theme[colors][surface2],
        )
    }
}

@Composable
context(store: IntentReceiver<ProjectListIntent>)
private fun ProjectFilters(state: ProjectListState) {
    LazyColumn(
        modifier = Modifier.width(225.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        state.selectedPlatform?.let { platform ->
            val groups = platform.categoryGroups[state.selectedType]
            groups?.forEachIndexed { index, group ->
                item(group) {
                    ProjectFilterCategory(
                        title = group.name,
                        modifier = if (index == groups.lastIndex) Modifier.padding(bottom = 8.dp) else Modifier
                    ) {
                        group.categories.forEach {
                            AppIconTextButton(
                                onClick = { store.intent(ProjectListIntent.OnCategoryClick(it)) },
                                painter = it.iconView.painter,
                                text = translateCategory(it),
                                backgroundColor = if (it in state.selectedCategories) Theme[colors][surface2] else Color.Unspecified,
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectFilterCategory(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth().appCard().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AppText(
            text = categoryText(title),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            content()
        }
    }
}

@Composable
context(
    _: IntentReceiver<ProjectListIntent>,
    _: AnimatedContentScope,
    _: SharedTransitionScope
)
private fun RowScope.ProjectList(state: ProjectListState, listState: LazyListState) {
    LazyColumn(
        state = listState,
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (val projects = state.projects) {
            is Resource.Loaded -> when (val result = projects.result) {
                is Result.Error -> item(result) { AppError(result.throwable) }
                is Result.Success -> result.data.forEach {
                    item(it.id) {
                        ProjectListItemCard(it)
                    }
                }
            }

            Resource.Loading -> repeat(20) {
                item {
                    ProjectListItemCard(null)
                }
            }

            Resource.Unloaded -> Unit
        }
    }
}

@Composable
context(
    store: IntentReceiver<ProjectListIntent>,
    animatedVisibilityScope: AnimatedContentScope,
    sharedTransitionScope: SharedTransitionScope
)
private fun LazyItemScope.ProjectListItemCard(
    project: BriefProjectView?
) = with(sharedTransitionScope) {
    if (project != null) {
        val interactionSource = remember { MutableInteractionSource() }

        ProjectListItemCardLayout(
            modifier = Modifier
                .hoverable(interactionSource = interactionSource)
                .appAnimateItem()
                .appSharedElement(
                    sharedContentState = rememberSharedContentState("project/${project.id}/card"),
                    animatedVisibilityScope = animatedVisibilityScope
                ),
            interactionSource = interactionSource,
            onClick = { store.intent(ProjectListIntent.OnProjectClick(project.id)) },
            icon = {
                Image(
                    painter = rememberAsyncImagePainter(project.logoUrl),
                    contentDescription = project.name,
                    modifier = it
                        .clip(RoundedCornerShape(4.dp))
                        .appSharedElement(
                            sharedContentState = contextOf<SharedTransitionScope>()
                                .rememberSharedContentState("project/${project.id}/logo"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            clipInOverlayDuringTransition = contextOf<SharedTransitionScope>()
                                .OverlayClip(RoundedCornerShape(4.dp))
                        ),
                    contentScale = ContentScale.Crop
                )
            },
            title = {
                AppText(
                    text = project.name,
                    fontWeight = FontWeight.SemiBold,
                    singleLine = true,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.appSharedElement(
                        sharedContentState = contextOf<SharedTransitionScope>()
                            .rememberSharedContentState("project/${project.id}/title"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                )
            },
            author = {
                AppText(
                    text = "by ${project.author}",
                    singleLine = true,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            description = {
                AppText(
                    text = project.description,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    fontSize = 12.sp,
                    modifier = Modifier.appSharedElement(
                        sharedContentState = contextOf<SharedTransitionScope>()
                            .rememberSharedContentState("project/${project.id}/description"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                )
            },
            downloads = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIcon(
                        imageVector = Lucide.Download,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    AppText(
                        text = stringResource(
                            Res.string.downloadCount,
                            HumanReadable.abbreviation(project.downloads, decimals = 1)
                        ),
                        singleLine = true,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            },
            updated = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIcon(
                        imageVector = Lucide.RefreshCw,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                    AppText(
                        text = stringResource(Res.string.lastUpdated, HumanReadable.timeAgo(project.lastUpdated)),
                        fontSize = 12.sp,
                        singleLine = true,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        )
    } else {
        ProjectListItemCardLayout(
            icon = { AppLoadingPlaceholder(it) },
            title = { AppLoadingTextPlaceholder(Modifier.width(200.dp)) },
            author = { },
            description = { AppLoadingTextPlaceholder(Modifier.width(300.dp)) },
            downloads = { AppLoadingTextPlaceholder(Modifier.width(100.dp)) },
            updated = { AppLoadingTextPlaceholder(Modifier.width(100.dp)) },
        )
    }
}

@Composable
context(
    _: AnimatedContentScope,
    _: SharedTransitionScope
)
private fun LazyItemScope.ProjectListItemCardLayout(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource? = null,
    onClick: (() -> Unit)? = null,
    icon: @Composable context(AnimatedContentScope, SharedTransitionScope) RowScope.(Modifier) -> Unit,
    title: @Composable context(AnimatedContentScope, SharedTransitionScope) RowScope.() -> Unit,
    author: @Composable context(AnimatedContentScope, SharedTransitionScope) RowScope.() -> Unit,
    description: @Composable context(AnimatedContentScope, SharedTransitionScope) ColumnScope.() -> Unit,
    downloads: @Composable context(AnimatedContentScope, SharedTransitionScope) ColumnScope.() -> Unit,
    updated: @Composable context(AnimatedContentScope, SharedTransitionScope) ColumnScope.() -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth()
            .appCard(interactionSource = interactionSource, onClick = onClick)
            .padding(8.dp)
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon(Modifier.size(64.dp))
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                title()
                author()
            }
            description()
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.End,
            modifier = Modifier.fillMaxHeight()
        ) {
            downloads()
            updated()
        }
    }
}

val ProjectType.displayName: StringResource
    get() = when (this) {
        ProjectType.Mod -> Res.string.mod
        ProjectType.Modpack -> Res.string.modpack
        ProjectType.ResourcePack -> Res.string.resourcePack
        ProjectType.ShaderPack -> Res.string.shaderPack
        ProjectType.DataPack -> Res.string.dataPack
        ProjectType.World -> Res.string.world
    }

val ProjectType.icon: ImageVector
    get() = when (this) {
        ProjectType.Mod -> Lucide.Box
        ProjectType.Modpack -> Lucide.PackageOpen
        ProjectType.ResourcePack -> Lucide.Paintbrush
        ProjectType.ShaderPack -> Lucide.Glasses
        ProjectType.DataPack -> Lucide.Braces
        ProjectType.World -> Lucide.Earth
    }

val IconView?.painter: Painter
    @Composable get() = when (this) {
        is IconView.Data -> LocalDensity.current.let { remember { svgString.toByteArray().decodeToSvgPainter(it) } }
        is IconView.Named -> rememberVectorPainter(getIconFromName(name))
        null -> rememberVectorPainter(Lucide.CircleQuestionMark)
    }

@Composable
private fun categoryText(category: String): String = when (category) {
    "categories" -> stringResource(Res.string.categories)
    "resolutions" -> stringResource(Res.string.resolutions)
    "features" -> stringResource(Res.string.features)
    "performance impact" -> stringResource(Res.string.performanceImpact)
    else -> category.replaceFirstChar { if (it.isLowerCase()) it.titlecase(getDefault()) else it.toString() }
}

@Composable
private fun translateCategory(category: ProjectCategoryView): String =
    Res.allStringResources[category.slugSnakeCase]?.let { stringResource(it) } ?: category.name ?: category.slug

private fun getIconFromName(name: String): ImageVector = when (name) {
    "compass" -> Lucide.Compass
    "cloud-sun-rain" -> Lucide.CloudSunRain
    "headphones" -> Lucide.Headphones
    "box" -> Lucide.Box
    "lamp" -> Lucide.Lamp
    "brush" -> Lucide.Brush
    "chart-no-axes-column-increasing" -> Lucide.ChartNoAxesColumnIncreasing
    "blend" -> Lucide.Blend
    "swords" -> Lucide.Swords
    "cpu" -> Lucide.Cpu
    "bug" -> Lucide.Bug
    "house" -> Lucide.House
    "dollar-sign" -> Lucide.DollarSign
    "sun" -> Lucide.Sun
    "wrench" -> Lucide.Wrench
    "wand-sparkles" -> Lucide.WandSparkles
    "tree-deciduous" -> Lucide.TreeDeciduous
    "type" -> Lucide.Type
    "carrot" -> Lucide.Carrot
    "sliders-vertical" -> Lucide.SlidersVertical
    "panels-top-left" -> Lucide.PanelsTopLeft
    "signal-high" -> Lucide.SignalHigh
    "pickaxe" -> Lucide.Pickaxe
    "book" -> Lucide.Book
    "feather" -> Lucide.Feather
    "globe" -> Lucide.Globe
    "signal-low" -> Lucide.SignalLow
    "wand" -> Lucide.Wand
    "server" -> Lucide.Server
    "signal-medium" -> Lucide.SignalMedium
    "award" -> Lucide.Award
    "square-plus" -> Lucide.SquarePlus
    "layers" -> Lucide.Layers
    "users" -> Lucide.Users
    "zap" -> Lucide.Zap
    "lightbulb" -> Lucide.Lightbulb
    "network" -> Lucide.Network
    "image" -> Lucide.Image
    "flip-horizontal-2" -> Lucide.FlipHorizontal2
    "film" -> Lucide.Film
    "mountain" -> Lucide.Mountain
    "hexagon" -> Lucide.Hexagon
    "message-circle" -> Lucide.MessageCircle
    "archive" -> Lucide.Archive
    "hard-drive" -> Lucide.HardDrive
    "pen-tool" -> Lucide.PenTool
    "truck" -> Lucide.Truck
    "square-pen" -> Lucide.SquarePen
    "toolbox" -> Lucide.ToolCase
    "ice-cream-cone" -> Lucide.IceCreamCone
    "earth" -> Lucide.Earth
    // CURSEFORGE
    "shield" -> Lucide.Shield
    "boxes" -> Lucide.Boxes
    "heart-crack" -> Lucide.HeartCrack
    "map" -> Lucide.Map
    "drone" -> Lucide.Drone
    "bug-off" -> Lucide.BugOff
    "blocks" -> Lucide.Blocks
    "graduation-cap" -> Lucide.GraduationCap
    "briefcase" -> Lucide.Briefcase
    "cable" -> Lucide.Cable
    "hand-fist" -> Lucide.HandFist
    "puzzle" -> Lucide.Puzzle
    "heart" -> Lucide.Heart
    "video" -> Lucide.Video
    "braces" -> Lucide.Braces
    "castle" -> Lucide.Castle
    "cog" -> Lucide.Cog
    "trophy" -> Lucide.Trophy
    else -> Lucide.CircleQuestionMark
}
