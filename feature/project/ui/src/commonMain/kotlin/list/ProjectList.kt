package app.vercors.feature.project.ui.list

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.vercors.core.domain.Page
import app.vercors.core.ui.animation.AppAnimatedContent
import app.vercors.core.ui.animation.AppAnimatedVisibility
import app.vercors.core.ui.component.*
import app.vercors.core.ui.layout.AppError
import app.vercors.core.ui.layout.AppFilterBar
import app.vercors.core.ui.layout.AppPaginator
import app.vercors.core.ui.modifier.appCard
import app.vercors.core.ui.modifier.scrollGradient
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.surface2
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.ui.*
import app.vercors.feature.project.domain.model.ProjectSearchError
import app.vercors.feature.project.domain.model.ProjectSortBy
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.ui.*
import app.vercors.feature.project.ui.details.isInstallable
import app.vercors.feature.project.ui.install.ProjectInstallButton
import app.vercors.feature.project.ui.install.ProjectInstallTarget
import app.vercors.shared.ui.*
import coil3.compose.rememberAsyncImagePainter
import com.composables.icons.lucide.*
import com.composeunstyled.theme.Theme
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.overlay.OverlayEffect
import com.slack.circuit.sharedelements.SharedElementTransitionScope
import dev.zacsweers.metro.AppScope
import opensavvy.state.progressive.*
import org.jetbrains.compose.resources.stringResource

@CircuitInject(ProjectListScreen::class, AppScope::class)
@Composable
fun ProjectList(
    store: Store<ProjectListState, ProjectListEvent>,
    modifier: Modifier = Modifier,
) {
    val (state, eventSink) = store

    with(eventSink) {
        state.installTarget?.let { target ->
            OverlayEffect {
                when (target) {
                    is ProjectInstallTarget.Project -> eventSink(ProjectListEvent.OnInstallResult(show(target.dialog)))
                    is ProjectInstallTarget.Modpack -> eventSink(ProjectListEvent.OnInstallModpackResult(show(target.dialog)))
                }
            }
        }

        Column(
            modifier = modifier.fillMaxSize().padding(top = 8.dp, start = 8.dp, end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProjectListHeader(
                filter = state.filter,
                providerList = state.platformList.valueOrNull
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
                    ProjectListPagination(state.filter, state.projects.valueOrNull?.total)
                    ProjectListContent(state.filter, state.projects)
                }
            }
        }
    }
}

@Composable
context(eventSink: EventSink<ProjectListEvent>)
private fun ProjectListHeader(
    filter: ProjectListFilter,
    providerList: List<ProjectPlatformView>?
) {
    AppFilterBar {
        AppSelectField(
            value = filter.selectedPlatform,
            items = providerList ?: emptyList(),
            onValueChange = { eventSink(ProjectListEvent.OnProjectPlatformChange(it!!)) },
            itemRenderer = { value, _ ->
                AppSelectItem(
                    text = value?.name ?: "",
                    icon = value.icon
                )
            },
            modifier = Modifier.width(150.dp)
        )

        AppSelectField(
            value = filter.selectedType ?: ProjectType.entries.first(),
            items = ProjectType.entries.filter { filter.selectedPlatform?.categoryGroups?.keys?.contains(it) == true },
            onValueChange = { eventSink(ProjectListEvent.OnProjectTypeChange(it)) },
            itemRenderer = { value, _ ->
                AppSelectItem(
                    text = value.displayName,
                    icon = value.icon
                )
            },
            modifier = Modifier.width(175.dp)
        )

        AppSearchField(
            initialValue = filter.searchQuery,
            onValueChange = { eventSink(ProjectListEvent.OnSearchQueryChange(it)) },
            debounceMillis = 750,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
context(_: EventSink<ProjectListEvent>)
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
context(eventSink: EventSink<ProjectListEvent>)
private fun ProjectLoaderFilter(selectedLoaders: List<ModLoaderType>) {
    ProjectFilterCategory(
        title = stringResource(GameRes.string.loader),
        hasSelectedItems = selectedLoaders.isNotEmpty(),
        selectedContent = {
            selectedLoaders.forEach { loader ->
                AppIconTextButton(
                    onClick = { eventSink(ProjectListEvent.OnLoaderClick(loader)) },
                    imageVector = loader.icon,
                    text = loader.name,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = ThinButtonContentPadding
                )
            }
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ModLoaderType.entries.forEach {
                AppIconTextButton(
                    onClick = { eventSink(ProjectListEvent.OnLoaderClick(it)) },
                    imageVector = it.icon,
                    text = it.name,
                    backgroundColor = if (it in selectedLoaders) Theme[colors][surface2] else Color.Unspecified,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = ThinButtonContentPadding
                )
            }
        }
    }
}

@Composable
context(eventSink: EventSink<ProjectListEvent>)
private fun ProjectVersionFilter(state: ProjectListState) {
    ProjectFilterCategory(
        title = stringResource(GameRes.string.gameVersion),
        hasSelectedItems = state.filter.selectedGameVersions.isNotEmpty(),
        selectedContent = {
            state.filter.selectedGameVersions.forEach {
                AppIconTextButton(
                    onClick = { eventSink(ProjectListEvent.OnGameVersionClick(it)) },
                    imageVector = null,
                    text = it.id,
                    horizontalArrangement = Arrangement.aligned(Alignment.Start),
                    contentPadding = ThinButtonContentPadding
                )
            }
        }
    ) {
        val listState = rememberLazyListState()

        AppSearchField(
            initialValue = state.gameVersionSearchQuery,
            onValueChange = { eventSink(ProjectListEvent.OnGameVersionSearchQueryChange(it)) },
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
                        onClick = { eventSink(ProjectListEvent.OnGameVersionClick(version)) },
                        imageVector = null,
                        backgroundColor = if (version in state.filter.selectedGameVersions) Theme[colors][surface2] else Color.Unspecified,
                        modifier = Modifier.fillMaxWidth(),
                        text = version.id,
                        horizontalArrangement = Arrangement.aligned(Alignment.Start),
                        contentPadding = ThinButtonContentPadding
                    )
                }
            }
            VerticalScrollbar(adapter = rememberScrollbarAdapter(listState))
        }
        AppCheckbox(
            label = stringResource(GameRes.string.showSnapshots),
            checked = state.showSnapshot,
            onCheckedChange = { eventSink(ProjectListEvent.OnShowSnapshotClick) },
            invert = true,
            checkboxSize = 20.dp,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
context(eventSink: EventSink<ProjectListEvent>)
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
                    onClick = { eventSink(ProjectListEvent.OnCategoryClick(it)) },
                    painter = it.iconView?.painter,
                    text = translateCategory(it),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = ThinButtonContentPadding
                )
            }
        }
    ) {
        group.categories.forEach {
            AppIconTextButton(
                onClick = { eventSink(ProjectListEvent.OnCategoryClick(it)) },
                painter = it.iconView?.painter,
                text = translateCategory(it),
                backgroundColor = if (it in selectedCategories) Theme[colors][surface2] else Color.Unspecified,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = ThinButtonContentPadding
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
context(eventSink: EventSink<ProjectListEvent>)
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
                onValueChange = { eventSink(ProjectListEvent.OnSortByChange(it)) },
                itemRenderer = { value, isButton ->
                    AppSelectItem(
                        text =
                            if (isButton) "${stringResource(SharedRes.string.sortBy)}: ${value.displayName}"
                            else value.displayName,
                    )
                },
                modifier = Modifier.width(200.dp)
            )
            AppSelectField(
                value = filter.pageSize,
                items = listOf(10, 20, 50),
                onValueChange = { eventSink(ProjectListEvent.OnPageSizeChange(it)) },
                itemRenderer = { value, isButton ->
                    AppSelectItem(
                        text =
                            if (isButton) "${stringResource(ProjectRes.string.itemsPerPage)}: $value"
                            else "$value",
                    )
                },
                modifier = Modifier.width(115.dp)
            )
            totalElements?.let { totalElements ->
                AppPaginator(
                    offset = filter.offset,
                    pageSize = filter.pageSize,
                    totalElements = totalElements,
                    onOffsetChange = { eventSink(ProjectListEvent.OnOffsetChange(it)) },
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
                        onClick = { eventSink(ProjectListEvent.OnLoaderClick(loader)) },
                        imageVector = Lucide.X,
                        text = loader.name,
                        contentPadding = ThinButtonContentPadding
                    )
                }
                filter.selectedGameVersions.forEach { version ->
                    AppIconTextButton(
                        onClick = { eventSink(ProjectListEvent.OnGameVersionClick(version)) },
                        imageVector = Lucide.X,
                        text = version.id,
                        contentPadding = ThinButtonContentPadding
                    )
                }
                filter.selectedCategories.forEach { category ->
                    AppIconTextButton(
                        onClick = { eventSink(ProjectListEvent.OnCategoryClick(category)) },
                        imageVector = Lucide.X,
                        text = translateCategory(category),
                        contentPadding = ThinButtonContentPadding
                    )
                }
            }
        }
    }
}

@Composable
context(_: EventSink<ProjectListEvent>)
private fun ColumnScope.ProjectListContent(
    filter: ProjectListFilter,
    projects: ProgressiveOutcome<ProjectSearchError, Page<BriefProjectView>>
) {
    val listState = rememberLazyListState()

    LaunchedEffect(filter, projects) {
        listState.animateScrollToItem(0)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        projects.onLoading {
            repeat(20) {
                item(it) {
                    ProjectListItemCard(null, it == 19)
                }
            }
        }
        projects.onSuccess { result ->
            if (result.items.isEmpty()) {
                item { NoProjectsFound() }
            }
            itemsIndexed(result.items, key = { _, it -> it.id }) { index, it ->
                ProjectListItemCard(it, index == result.items.lastIndex)
            }
        }
        projects.onFailure {
            item(it) { AppError(it.message) }
        }
    }
}

@Composable
fun NoProjectsFound() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        AppText(text = stringResource(ProjectRes.string.noProjectsFound))
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
context(eventSink: EventSink<ProjectListEvent>)
private fun LazyItemScope.ProjectListItemCard(
    project: BriefProjectView?,
    isLast: Boolean
) = SharedElementTransitionScope {
    if (project != null) {
        val interactionSource = remember { MutableInteractionSource() }

        ProjectListItemCardLayout(
            modifier = (if (isLast) Modifier.padding(bottom = 8.dp) else Modifier)
                .hoverable(interactionSource = interactionSource)
                .projectSharedElement(project.id, "card", ProjectSharedElementOrigin.ProjectList),
            interactionSource = interactionSource,
            onClick = { eventSink(ProjectListEvent.OnProjectClick(project.id)) },
            contextMenuItems = projectContextMenuItems(
                project = project,
                onInstallClick = { eventSink(ProjectListEvent.OnInstallProjectClick(it)) },
                onProjectClick = { eventSink(ProjectListEvent.OnProjectClick(it)) },
            ),
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
                    text = stringResource(SharedRes.string.byAuthor, project.author),
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
                        .projectSharedElement(project.id, "description", ProjectSharedElementOrigin.ProjectList),
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
                        text = stringResource(ProjectRes.string.downloadCount, project.downloadsReadable),
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
                        imageVector = Lucide.History,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                    AppText(
                        text = stringResource(ProjectRes.string.lastUpdated, project.lastUpdatedReadable),
                        fontSize = 12.sp,
                        singleLine = true,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            },
            installButton = if (project.type.isInstallable) {
                { ProjectInstallButton(onClick = { eventSink(ProjectListEvent.OnInstallProjectClick(project)) }) }
            } else {
                null
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
            installButton = null,
        )
    }
}

@Composable
context(_: SharedElementTransitionScope)
private fun LazyItemScope.ProjectListItemCardLayout(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource? = null,
    onClick: (() -> Unit)? = null,
    contextMenuItems: () -> List<ContextMenuItem> = { emptyList() },
    icon: @Composable context(SharedElementTransitionScope) RowScope.(Modifier) -> Unit,
    title: @Composable context(SharedElementTransitionScope) RowScope.() -> Unit,
    author: @Composable context(SharedElementTransitionScope) RowScope.() -> Unit,
    description: @Composable context(SharedElementTransitionScope) ColumnScope.() -> Unit,
    downloads: @Composable context(SharedElementTransitionScope) ColumnScope.() -> Unit,
    updated: @Composable context(SharedElementTransitionScope) ColumnScope.() -> Unit,
    installButton: (@Composable () -> Unit)? = null,
) {
    val hovered: Boolean by interactionSource?.collectIsHoveredAsState() ?: remember { mutableStateOf(false) }
    ContextMenuArea(items = contextMenuItems) {
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
            if (installButton == null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    downloads()
                    updated()
                }
            } else {
                AppAnimatedContent(
                    targetState = hovered,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    if (it) {
                        Box(contentAlignment = Alignment.CenterEnd, modifier = Modifier.fillMaxHeight()) {
                            installButton()
                        }
                    } else {
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
            }
        }
    }
}
