package app.vercors.feature.instance.ui.details.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.vercors.core.ui.component.*
import app.vercors.core.ui.layout.AppLoading
import app.vercors.core.ui.modifier.appCard
import app.vercors.core.ui.theme.*
import app.vercors.feature.instance.domain.model.InstanceFolder
import app.vercors.feature.instance.ui.*
import app.vercors.feature.instance.ui.details.InstanceDetailsEvent
import app.vercors.feature.instance.ui.details.InstanceView
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.shared.ui.EventSink
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.sortBy
import coil3.compose.rememberAsyncImagePainter
import com.composables.icons.lucide.*
import com.composeunstyled.theme.Theme
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path
import okio.Path.Companion.toPath
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

private enum class ContentSortBy {
    Name,
    LastModified
}

private val ContentSortBy.displayName: String
    @Composable get() = stringResource(
        when (this) {
            ContentSortBy.Name -> InstanceRes.string.sortByName
            ContentSortBy.LastModified -> InstanceRes.string.sortByLastModified
        }
    )

@Composable
context(eventSink: EventSink<InstalledContentEvent>)
private fun InstalledContentFileList(
    files: List<InstalledContentFileView>?,
    emptyMessage: StringResource,
    searchPlaceholder: StringResource,
    addLabel: StringResource,
    addEnabled: Boolean,
    importLabel: StringResource,
    importExtension: String,
    openFolderLabel: StringResource,
    contentFolder: InstanceFolder,
    onAddClick: () -> Unit,
    onOpenProject: (platformId: String, projectId: String) -> Unit,
    onOpenFolder: (InstanceFolder) -> Unit,
    horizontalTabs: Boolean,
) {
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyEnabled by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf(ContentSortBy.Name) }
    val listState = rememberLazyListState()

    LaunchedEffect(searchQuery, showOnlyEnabled, sortBy) {
        listState.scrollToItem(0)
    }

    Column(modifier = Modifier.fillMaxSize().let { if (!horizontalTabs) it.padding(vertical = 8.dp) else it }
        .appCard()) {
        InstalledContentFileListHeader(
            searchQuery = searchQuery,
            searchPlaceholder = searchPlaceholder,
            onSearchQueryChange = { searchQuery = it },
            showOnlyEnabled = showOnlyEnabled,
            onShowOnlyEnabledChange = { showOnlyEnabled = it },
            sortBy = sortBy,
            onSortByChange = { sortBy = it },
            addLabel = addLabel,
            addEnabled = addEnabled,
            importLabel = importLabel,
            importExtension = importExtension,
            openFolderLabel = openFolderLabel,
            contentFolder = contentFolder,
            onAddClick = onAddClick,
            onOpenFolder = onOpenFolder,
        )

        val filteredFiles = files?.filteredAndSorted(searchQuery, showOnlyEnabled, sortBy)
        InstalledContentFileListBody(
            files = files,
            filteredFiles = filteredFiles,
            emptyMessage = emptyMessage,
            listState = listState,
            onOpenProject = onOpenProject
        )
    }
}

@Composable
context(eventSink: EventSink<InstalledContentEvent>)
private fun InstalledContentFileListHeader(
    searchQuery: String,
    searchPlaceholder: StringResource,
    onSearchQueryChange: (String) -> Unit,
    showOnlyEnabled: Boolean,
    onShowOnlyEnabledChange: (Boolean) -> Unit,
    sortBy: ContentSortBy,
    onSortByChange: (ContentSortBy) -> Unit,
    addLabel: StringResource,
    addEnabled: Boolean,
    importLabel: StringResource,
    importExtension: String,
    openFolderLabel: StringResource,
    contentFolder: InstanceFolder,
    onAddClick: () -> Unit,
    onOpenFolder: (InstanceFolder) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (addEnabled) {
            AppIconTextButton(
                onClick = onAddClick,
                text = stringResource(addLabel),
                imageVector = Lucide.Plus,
                backgroundColor = Theme[colors][primary],
                contentColor = Theme[colors][onPrimary],
            )
        }
        AppSearchField(
            initialValue = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = stringResource(searchPlaceholder),
            modifier = Modifier.weight(1f)
        )
        AppCheckbox(
            label = stringResource(InstanceRes.string.showOnlyEnabled),
            checked = showOnlyEnabled,
            onCheckedChange = onShowOnlyEnabledChange
        )
        AppSelectField(
            value = sortBy,
            items = ContentSortBy.entries,
            onValueChange = onSortByChange,
            itemRenderer = { value, isButton ->
                AppSelectItem(
                    text = if (isButton) "${stringResource(SharedRes.string.sortBy)}: ${value.displayName}"
                    else value.displayName
                )
            },
            modifier = Modifier.width(200.dp)
        )
        InstalledContentFileListMenu(
            addLabel = addLabel,
            addEnabled = addEnabled,
            importLabel = importLabel,
            importExtension = importExtension,
            openFolderLabel = openFolderLabel,
            contentFolder = contentFolder,
            onAddClick = onAddClick,
            onOpenFolder = onOpenFolder
        )
    }
}

@Composable
context(eventSink: EventSink<InstalledContentEvent>)
private fun InstalledContentFileListMenu(
    addLabel: StringResource,
    addEnabled: Boolean,
    importLabel: StringResource,
    importExtension: String,
    openFolderLabel: StringResource,
    contentFolder: InstanceFolder,
    onAddClick: () -> Unit,
    onOpenFolder: (InstanceFolder) -> Unit
) {
    val importPickerLauncher = rememberFilePickerLauncher(type = FileKitType.File(importExtension)) { file ->
        file?.let { eventSink(InstalledContentEvent.OnImportClick(it.path)) }
    }
    AppMenuButton {
        AppMenuItem(
            onClick = onAddClick,
            text = stringResource(addLabel),
            imageVector = Lucide.Plus,
            enabled = addEnabled
        )
        AppMenuItem(
            onClick = importPickerLauncher::launch,
            text = stringResource(importLabel),
            imageVector = Lucide.FileUp,
            enabled = addEnabled
        )
        AppMenuItem(
            onClick = { onOpenFolder(contentFolder) },
            text = stringResource(openFolderLabel),
            imageVector = Lucide.FolderOpen
        )
        if (contentFolder == InstanceFolder.Mods) {
            AppMenuItem(
                onClick = { onOpenFolder(InstanceFolder.Config) },
                text = stringResource(InstanceRes.string.openConfigFolder),
                imageVector = Lucide.FolderCog
            )
        }
    }
}

private fun List<InstalledContentFileView>.filteredAndSorted(
    searchQuery: String,
    showOnlyEnabled: Boolean,
    sortBy: ContentSortBy
): List<InstalledContentFileView> = filter {
    (searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true)) &&
            (!showOnlyEnabled || it.enabled)
}.let { list ->
    when (sortBy) {
        ContentSortBy.Name -> list.sortedBy { it.title.lowercase() }
        ContentSortBy.LastModified -> list.sortedByDescending { it.lastModified ?: Instant.DISTANT_PAST }
    }
}

@Composable
context(eventSink: EventSink<InstalledContentEvent>)
private fun InstalledContentFileListBody(
    files: List<InstalledContentFileView>?,
    filteredFiles: List<InstalledContentFileView>?,
    emptyMessage: StringResource,
    listState: LazyListState,
    onOpenProject: (platformId: String, projectId: String) -> Unit
) {
    when {
        files == null -> AppLoading()
        files.isEmpty() -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AppText(stringResource(emptyMessage))
        }

        filteredFiles.isNullOrEmpty() -> AppText(stringResource(InstanceRes.string.noMatchingFiles))
        else -> Row(modifier = Modifier.fillMaxSize().padding(end = 4.dp)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(filteredFiles, key = { it.fileName }) { file ->
                    InstalledContentFileRow(file, onOpenProject)
                }
            }
            VerticalScrollbar(adapter = rememberScrollbarAdapter(listState))
        }
    }
}

@Composable
context(eventSink: EventSink<InstalledContentEvent>)
private fun InstalledContentFileRow(
    file: InstalledContentFileView,
    onOpenProject: (platformId: String, projectId: String) -> Unit
) {
    val contentAlpha = if (file.enabled) 1f else 0.4f

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (file.iconFilePath != null) {
            Image(
                painter = rememberAsyncImagePainter(model = file.iconFilePath.toPath()),
                contentDescription = file.title,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)).alpha(contentAlpha),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)).alpha(contentAlpha)
                    .background(Theme[colors][surface2]),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(imageVector = Lucide.Box, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }

        Column(
            modifier = Modifier.weight(1f).alpha(contentAlpha),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppText(
                    text = file.title,
                    fontWeight = FontWeight.SemiBold,
                    singleLine = true,
                    overflow = TextOverflow.Ellipsis
                )
                file.version?.let {
                    AppText(text = it, fontSize = 12.sp, color = Theme[colors][onBackground].copy(alpha = 0.6f))
                }
            }
            file.description?.let {
                AppText(text = it, fontSize = 12.sp, singleLine = true, overflow = TextOverflow.Ellipsis)
            }
        }

        AppMenuButton {
            AppMenuItem(
                onClick = { eventSink(InstalledContentEvent.OnEnabledChange(file.fileName, !file.enabled)) },
                text = stringResource(if (file.enabled) InstanceRes.string.disable else InstanceRes.string.enable),
                imageVector = if (file.enabled) Lucide.EyeOff else Lucide.Eye
            )
            AppMenuItem(
                onClick = {
                    eventSink(InstalledContentEvent.OnRemoveClick(file.fileName, file.title))
                },
                text = stringResource(InstanceRes.string.remove),
                imageVector = Lucide.Trash,
                contentColor = Theme[colors][error]
            )
            file.providers.forEach { provider ->
                AppMenuItem(
                    onClick = {
                        onOpenProject(provider.platformId, provider.projectId)
                    },
                    text = stringResource(
                        InstanceRes.string.openProject,
                        provider.platformId.toPlatformDisplayName()
                    ),
                    imageVector = Lucide.ArrowRight
                )
            }
        }
    }
}

@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
fun InstalledMods(
    instance: InstanceView,
    content: Store<InstalledContentState, InstalledContentEvent>,
    horizontalTabs: Boolean
) {
    with(content.eventSink) {
        InstalledContentFileList(
            files = content.state.files,
            emptyMessage = InstanceRes.string.noModsInstalled,
            searchPlaceholder = InstanceRes.string.searchInstalledMods,
            addLabel = InstanceRes.string.addMod,
            addEnabled = instance.loaderType != null,
            importLabel = InstanceRes.string.importMod,
            importExtension = "jar",
            openFolderLabel = InstanceRes.string.openModsFolder,
            contentFolder = InstanceFolder.Mods,
            onAddClick = { eventSink(InstanceDetailsEvent.OnAddContentClick(ProjectType.Mod)) },
            onOpenProject = { platformId, projectId ->
                eventSink(InstanceDetailsEvent.OnOpenProjectClick(platformId, projectId))
            },
            onOpenFolder = { folder -> eventSink(InstanceDetailsEvent.OnOpenInstanceFolderClick(folder)) },
            horizontalTabs = horizontalTabs
        )
    }
}

@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
fun InstalledResourcePacks(
    content: Store<InstalledContentState, InstalledContentEvent>,
    horizontalTabs: Boolean
) {
    with(content.eventSink) {
        InstalledContentFileList(
            files = content.state.files,
            emptyMessage = InstanceRes.string.noResourcePacksInstalled,
            searchPlaceholder = InstanceRes.string.searchInstalledResourcePacks,
            addLabel = InstanceRes.string.addResourcePack,
            addEnabled = true,
            importLabel = InstanceRes.string.importResourcePack,
            importExtension = "zip",
            openFolderLabel = InstanceRes.string.openResourcePacksFolder,
            contentFolder = InstanceFolder.ResourcePacks,
            onAddClick = { eventSink(InstanceDetailsEvent.OnAddContentClick(ProjectType.ResourcePack)) },
            onOpenProject = { platformId, projectId ->
                eventSink(InstanceDetailsEvent.OnOpenProjectClick(platformId, projectId))
            },
            onOpenFolder = { folder -> eventSink(InstanceDetailsEvent.OnOpenInstanceFolderClick(folder)) },
            horizontalTabs = horizontalTabs,
        )
    }
}

@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
fun InstalledShaderPacks(
    content: Store<InstalledContentState, InstalledContentEvent>,
    horizontalTabs: Boolean
) {
    with(content.eventSink) {
        InstalledContentFileList(
            files = content.state.files,
            emptyMessage = InstanceRes.string.noShaderPacksInstalled,
            searchPlaceholder = InstanceRes.string.searchInstalledShaderPacks,
            addLabel = InstanceRes.string.addShaderPack,
            addEnabled = true,
            importLabel = InstanceRes.string.importShaderPack,
            importExtension = "zip",
            openFolderLabel = InstanceRes.string.openShaderPacksFolder,
            contentFolder = InstanceFolder.ShaderPacks,
            onAddClick = { eventSink(InstanceDetailsEvent.OnAddContentClick(ProjectType.ShaderPack)) },
            onOpenProject = { platformId, projectId ->
                eventSink(InstanceDetailsEvent.OnOpenProjectClick(platformId, projectId))
            },
            onOpenFolder = { folder -> eventSink(InstanceDetailsEvent.OnOpenInstanceFolderClick(folder)) },
            horizontalTabs = horizontalTabs,
        )
    }
}
