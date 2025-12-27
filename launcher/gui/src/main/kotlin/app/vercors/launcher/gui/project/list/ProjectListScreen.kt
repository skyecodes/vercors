package app.vercors.launcher.gui.project.list

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.vercors.launcher.gui.project.BriefProjectView
import app.vercors.launcher.gui.project.ProjectSharedElementOrigin
import app.vercors.launcher.gui.project.projectSharedElement
import app.vercors.launcher.gui.res.*
import app.vercors.shared.domain.Resource
import app.vercors.shared.domain.Result
import app.vercors.shared.domain.getOrNull
import app.vercors.shared.domain.loader.ModLoaderType
import app.vercors.shared.domain.project.model.ProjectId
import app.vercors.shared.domain.project.model.ProjectSortBy
import app.vercors.shared.domain.project.model.ProjectType
import app.vercors.shared.ui.core.animation.AppAnimatedVisibility
import app.vercors.shared.ui.core.animation.appAnimateItem
import app.vercors.shared.ui.core.component.*
import app.vercors.shared.ui.core.layout.AppError
import app.vercors.shared.ui.core.layout.AppPaginator
import app.vercors.shared.ui.core.modifier.appCard
import app.vercors.shared.ui.core.modifier.scrollGradient
import app.vercors.shared.ui.core.theme.colors
import app.vercors.shared.ui.core.theme.surface2
import app.vercors.shared.ui.loader.icon
import app.vercors.shared.ui.project.displayName
import coil3.compose.rememberAsyncImagePainter
import com.composables.icons.fontawesome.FontAwesome
import com.composables.icons.fontawesome.brands.Twitch
import com.composables.icons.lucide.*
import com.composeunstyled.theme.Theme
import nl.jacobras.humanreadable.HumanReadable
import org.jetbrains.compose.resources.decodeToSvgPainter
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
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
    val state by subscribe { action ->
        when (action) {
            is ProjectListAction.NavigateToProjectDetails -> navigateToProjectDetails(action.projectId)
        }
    }

    ProjectListScreen(state)
}

@Composable
context(
    _: IntentReceiver<ProjectListIntent>,
    _: AnimatedContentScope,
    _: SharedTransitionScope
)
private fun ProjectListScreen(state: ProjectListState) {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 8.dp, start = 8.dp, end = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ProjectListHeader(
            filter = state.filter,
            providerList = state.providerList.getOrNull()
        )
        Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProjectListFilters(state, state.filter)
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProjectListPagination(state.filter, state.totalElements)
                ProjectList(state.projects)
            }
        }
    }
}

@Composable
context(store: IntentReceiver<ProjectListIntent>)
private fun ProjectListHeader(
    filter: ProjectListFilter,
    providerList: List<ProjectPlatformView>?
) {
    Row(
        modifier = Modifier.appCard().padding(8.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AppSelectField(
            value = filter.selectedPlatform,
            items = providerList ?: emptyList(),
            onValueChange = { store.intent(ProjectListIntent.OnProjectProviderChange(it!!)) },
            itemRenderer = { value, _ ->
                AppSelectItem(
                    text = value?.name ?: "",
                    icon = value.icon
                )
            },
            backgroundColor = Theme[colors][surface2],
            modifier = Modifier.width(150.dp)
        )

        AppSelectField(
            value = filter.selectedType ?: ProjectType.entries.first(),
            items = ProjectType.entries.filter { filter.selectedPlatform?.categoryGroups?.keys?.contains(it) == true },
            onValueChange = { store.intent(ProjectListIntent.OnProjectTypeChange(it)) },
            itemRenderer = { value, _ ->
                AppSelectItem(
                    text = value.displayName,
                    icon = value.icon
                )
            },
            backgroundColor = Theme[colors][surface2],
            modifier = Modifier.width(175.dp)
        )

        AppTextField(
            placeholder = { AppText(stringResource(Res.string.search)) },
            initialValue = filter.searchQuery,
            onValueChange = { store.intent(ProjectListIntent.OnSearchQueryChange(it)) },
            debounceMillis = 750,
            imeAction = ImeAction.Search,
            backgroundColor = Theme[colors][surface2],
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
context(store: IntentReceiver<ProjectListIntent>)
private fun ProjectListFilters(state: ProjectListState, filter: ProjectListFilter) {
    val listState = rememberLazyListState()

    LaunchedEffect(filter.selectedType) {
        listState.scrollToItem(0)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.width(225.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (filter.selectedType?.requiresLoader == true) {
            item("loader") {
                ProjectLoaderFilter(filter.selectedLoaders)
            }
        }

        item("version") {
            ProjectVersionFilter(state)
        }

        filter.selectedPlatform?.let { platform ->
            val groups = platform.categoryGroups[filter.selectedType]
            if (groups != null) {
                itemsIndexed(groups, key = { _, group -> group }) { index, group ->
                    ProjectCategoryFilter(
                        group = group,
                        index = index,
                        groups = groups,
                        selectedCategories = filter.selectedCategories
                    )
                }
            }
        }
    }
}

@Composable
context(store: IntentReceiver<ProjectListIntent>)
private fun ProjectLoaderFilter(selectedLoaders: List<ModLoaderType>) {
    ProjectFilterCategory(
        title = stringResource(Res.string.loader),
        hasSelectedItems = selectedLoaders.isNotEmpty(),
        selectedContent = {
            selectedLoaders.forEach { loader ->
                AppIconTextButton(
                    onClick = { store.intent(ProjectListIntent.OnLoaderClick(loader)) },
                    imageVector = loader.icon,
                    text = loader.name,
                    backgroundColor = Theme[colors][surface2],
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                )
            }
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ModLoaderType.entries.forEach {
                AppIconTextButton(
                    onClick = { store.intent(ProjectListIntent.OnLoaderClick(it)) },
                    imageVector = it.icon,
                    text = it.name,
                    backgroundColor = if (it in selectedLoaders) Theme[colors][surface2] else Color.Unspecified,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                )
            }
        }
    }
}

@Composable
context(store: IntentReceiver<ProjectListIntent>)
private fun ProjectVersionFilter(state: ProjectListState) {
    ProjectFilterCategory(
        title = stringResource(Res.string.gameVersion),
        hasSelectedItems = state.filter.selectedGameVersions.isNotEmpty(),
        selectedContent = {
            state.filter.selectedGameVersions.forEach {
                AppIconTextButton(
                    onClick = { store.intent(ProjectListIntent.OnGameVersionClick(it)) },
                    imageVector = null,
                    backgroundColor = Theme[colors][surface2],
                    text = it.id,
                    horizontalArrangement = Arrangement.aligned(Alignment.Start)
                )
            }
        }
    ) {
        val listState = rememberLazyListState()

        AppTextField(
            initialValue = state.gameVersionSearchQuery,
            onValueChange = { store.intent(ProjectListIntent.OnGameVersionSearchQueryChange(it)) },
            placeholder = { AppText(stringResource(Res.string.search)) },
            backgroundColor = Theme[colors][surface2],
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).padding(top = 6.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).scrollGradient(
                    listState = listState,
                    orientation = Orientation.Vertical,
                ),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(state.filteredGameVersionList) { version ->
                    AppIconTextButton(
                        onClick = { store.intent(ProjectListIntent.OnGameVersionClick(version)) },
                        imageVector = null,
                        backgroundColor = if (version in state.filter.selectedGameVersions) Theme[colors][surface2] else Color.Unspecified,
                        modifier = Modifier.fillMaxWidth(),
                        text = version.id,
                        horizontalArrangement = Arrangement.aligned(Alignment.Start)
                    )
                }
            }
            VerticalScrollbar(adapter = rememberScrollbarAdapter(listState))
        }
        AppCheckbox(
            label = stringResource(Res.string.showSnapshots),
            checked = state.showSnapshot,
            onCheckedChange = { store.intent(ProjectListIntent.OnShowSnapshotClick) },
            invert = true,
            backgroundColor = Theme[colors][surface2],
            checkboxSize = 20.dp,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
context(store: IntentReceiver<ProjectListIntent>)
private fun ProjectCategoryFilter(
    group: ProjectCategoryGroupView,
    index: Int,
    groups: List<ProjectCategoryGroupView>,
    selectedCategories: List<ProjectCategoryView>
) {
    ProjectFilterCategory(
        title = group.name,
        modifier = if (index == groups.lastIndex) Modifier.padding(bottom = 8.dp) else Modifier,
        hasSelectedItems = selectedCategories.isNotEmpty(),
        selectedContent = {
            selectedCategories.forEach {
                AppIconTextButton(
                    onClick = { store.intent(ProjectListIntent.OnCategoryClick(it)) },
                    painter = it.iconView?.painter,
                    text = translateCategory(it),
                    backgroundColor = Theme[colors][surface2],
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                )
            }
        }
    ) {
        group.categories.forEach {
            AppIconTextButton(
                onClick = { store.intent(ProjectListIntent.OnCategoryClick(it)) },
                painter = it.iconView?.painter,
                text = translateCategory(it),
                backgroundColor = if (it in selectedCategories) Theme[colors][surface2] else Color.Unspecified,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            )
        }
    }
}

@Composable
fun ProjectFilterCategory(
    title: String,
    modifier: Modifier = Modifier,
    hasSelectedItems: Boolean = false,
    selectedContent: @Composable FlowRowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    var toggled by remember { mutableStateOf(true) }

    Column(
        modifier = modifier.fillMaxWidth().appCard().padding(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).clickable(
                interactionSource = null,
                indication = null,
                onClick = { toggled = !toggled }
            ).pointerHoverIcon(PointerIcon.Hand),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AppText(
                text = categoryText(title),
                fontWeight = FontWeight.SemiBold,
            )

            AppIcon(
                imageVector = if (toggled) Lucide.ChevronUp else Lucide.ChevronDown,
                contentDescription = null
            )
        }

        if (hasSelectedItems) {
            AppAnimatedVisibility(
                visible = !toggled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    selectedContent()
                }
            }
        }

        AppAnimatedVisibility(
            visible = toggled,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
context(store: IntentReceiver<ProjectListIntent>)
private fun ProjectListPagination(filter: ProjectListFilter, totalElements: Int?) {
    Column(
        modifier = Modifier.fillMaxWidth().appCard().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            itemVerticalAlignment = Alignment.CenterVertically
        ) {
            AppSelectField(
                value = filter.sortBy,
                items = ProjectSortBy.entries,
                onValueChange = { store.intent(ProjectListIntent.OnSortByChange(it)) },
                itemRenderer = { value, isButton ->
                    AppSelectItem(
                        text =
                            if (isButton) "${stringResource(Res.string.sortBy)}: ${value.displayName}"
                            else value.displayName,
                    )
                },
                backgroundColor = Theme[colors][surface2],
                modifier = Modifier.width(200.dp)
            )
            AppSelectField(
                value = filter.pageSize,
                items = listOf(10, 20, 50),
                onValueChange = { store.intent(ProjectListIntent.OnPageSizeChange(it)) },
                itemRenderer = { value, isButton ->
                    AppSelectItem(
                        text =
                            if (isButton) "${stringResource(Res.string.itemsPerPage)}: $value"
                            else "$value",
                    )
                },
                backgroundColor = Theme[colors][surface2],
                modifier = Modifier.width(115.dp)
            )
            totalElements?.let { totalElements ->
                AppPaginator(
                    offset = filter.offset,
                    pageSize = filter.pageSize,
                    totalElements = totalElements,
                    onOffsetChange = { store.intent(ProjectListIntent.OnOffsetChange(it)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (filter.selectedLoaders.isNotEmpty() || filter.selectedGameVersions.isNotEmpty() || filter.selectedCategories.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                filter.selectedLoaders.forEach { loader ->
                    AppIconTextButton(
                        onClick = { store.intent(ProjectListIntent.OnLoaderClick(loader)) },
                        imageVector = Lucide.X,
                        text = loader.name,
                        backgroundColor = Theme[colors][surface2],
                    )
                }
                filter.selectedGameVersions.forEach { version ->
                    AppIconTextButton(
                        onClick = { store.intent(ProjectListIntent.OnGameVersionClick(version)) },
                        imageVector = Lucide.X,
                        text = version.id,
                        backgroundColor = Theme[colors][surface2],
                    )
                }
                filter.selectedCategories.forEach { category ->
                    AppIconTextButton(
                        onClick = { store.intent(ProjectListIntent.OnCategoryClick(category)) },
                        imageVector = Lucide.X,
                        text = translateCategory(category),
                        backgroundColor = Theme[colors][surface2],
                    )
                }
            }
        }
    }
}

@Composable
context(
    _: IntentReceiver<ProjectListIntent>,
    _: AnimatedContentScope,
    _: SharedTransitionScope
)
private fun ColumnScope.ProjectList(projects: Resource<List<BriefProjectView>>) {
    val listState = rememberLazyListState()

    LaunchedEffect(projects) {
        listState.scrollToItem(0)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (projects) {
            is Resource.Loaded -> when (val result = projects.result) {
                is Result.Error -> item(result) { AppError(result.throwable) }
                is Result.Success -> {
                    if (result.data.isEmpty()) {
                        item { NoProjectsFound() }
                    }
                    itemsIndexed(result.data, key = { _, it -> it.id }) { index, it ->
                        ProjectListItemCard(it, index == result.data.lastIndex)
                    }
                }
            }

            Resource.Loading -> repeat(20) {
                item {
                    ProjectListItemCard(null, it == 19)
                }
            }

            Resource.Unloaded -> Unit
        }
    }
}

@Composable
context(store: IntentReceiver<ProjectListIntent>)
fun NoProjectsFound() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        AppText(text = stringResource(Res.string.noProjectsFound))
    }
}

@Composable
context(
    store: IntentReceiver<ProjectListIntent>,
    animatedVisibilityScope: AnimatedContentScope,
    sharedTransitionScope: SharedTransitionScope
)
private fun LazyItemScope.ProjectListItemCard(
    project: BriefProjectView?,
    isLast: Boolean
) = with(sharedTransitionScope) {
    if (project != null) {
        val interactionSource = remember { MutableInteractionSource() }

        ProjectListItemCardLayout(
            modifier = (if (isLast) Modifier.padding(bottom = 8.dp) else Modifier)
                .hoverable(interactionSource = interactionSource)
                .appAnimateItem()
                .projectSharedElement(project.id, "card", ProjectSharedElementOrigin.ProjectList),
            interactionSource = interactionSource,
            onClick = { store.intent(ProjectListIntent.OnProjectClick(project.id)) },
            icon = {
                Image(
                    painter = rememberAsyncImagePainter(project.logoUrl),
                    contentDescription = project.name,
                    modifier = it
                        .projectSharedElement(project.id, "logo", ProjectSharedElementOrigin.ProjectList)
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
                        .projectSharedElement(project.id, "title", ProjectSharedElementOrigin.ProjectList)
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
                    modifier = Modifier
                        .projectSharedElement(project.id, "description", ProjectSharedElementOrigin.ProjectList)
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

val ProjectType.displayName: String
    @Composable get() = stringResource(
        when (this) {
            ProjectType.Mod -> Res.string.mod
            ProjectType.Modpack -> Res.string.modpack
            ProjectType.ResourcePack -> Res.string.resourcePack
            ProjectType.ShaderPack -> Res.string.shaderPack
            ProjectType.DataPack -> Res.string.dataPack
            ProjectType.World -> Res.string.world
        }
    )

val ProjectType.icon: ImageVector
    get() = when (this) {
        ProjectType.Mod -> Lucide.Box
        ProjectType.Modpack -> Lucide.PackageOpen
        ProjectType.ResourcePack -> Lucide.Paintbrush
        ProjectType.ShaderPack -> Lucide.Glasses
        ProjectType.DataPack -> Lucide.Braces
        ProjectType.World -> Lucide.Earth
    }

val IconView.painter: Painter
    @Composable get() = when (this) {
        is IconView.Data -> LocalDensity.current.let { remember { svgString.toByteArray().decodeToSvgPainter(it) } }
        is IconView.Named -> rememberVectorPainter(getIconFromName(name))
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

@Composable
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
    "badge-check" -> Lucide.BadgeCheck
    "twitch" -> FontAwesome.Brands.Twitch
    "creeper" -> vectorResource(Res.drawable.creeper)
    "path-tracing" -> vectorResource(Res.drawable.path_tracing)
    "potato" -> vectorResource(Res.drawable.potato)
    "sink" -> vectorResource(Res.drawable.sink)
    "mcreator" -> vectorResource(Res.drawable.mcreator)
    else -> Lucide.CircleQuestionMark
}

val ProjectPlatformView?.icon: ImageVector
    @Composable get() = when (this?.id) {
        "modrinth" -> vectorResource(Res.drawable.modrinth)
        "curseforge" -> vectorResource(Res.drawable.curseforge)
        else -> Lucide.CircleQuestionMark
    }
