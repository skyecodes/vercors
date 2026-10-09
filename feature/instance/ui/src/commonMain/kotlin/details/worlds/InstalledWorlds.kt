package app.vercors.feature.instance.ui.details.worlds

import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.ui.*
import app.vercors.feature.instance.ui.details.InstanceDetailsEvent
import app.vercors.feature.instance.ui.details.InstanceView
import app.vercors.feature.launcher.domain.model.QuickPlayTarget
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.shared.ui.EventSink
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.sortBy
import coil3.compose.rememberAsyncImagePainter
import com.composables.icons.lucide.*
import com.composeunstyled.ProvideContentColor
import com.composeunstyled.theme.Theme
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path
import okio.Path.Companion.toPath
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
fun InstalledWorlds(
    instance: InstanceView,
    content: Store<InstalledWorldsState, InstalledWorldsEvent>,
    horizontalTabs: Boolean
) {
    val canQuickJoin = instance.status is InstanceStatus.Idle || instance.status is InstanceStatus.Failed
    with(content.eventSink) {
        InstalledWorldList(
            worlds = content.state.worlds,
            canQuickJoin = canQuickJoin,
            onAddClick = { eventSink(InstanceDetailsEvent.OnAddContentClick(ProjectType.World)) },
            onOpenFolder = { eventSink(InstanceDetailsEvent.OnOpenInstanceFolderClick(InstanceFolder.Saves)) },
            onQuickJoinClick = { folderName ->
                eventSink(InstanceDetailsEvent.OnQuickJoinClick(QuickPlayTarget.Singleplayer(folderName)))
            },
            horizontalTabs = horizontalTabs
        )
    }
}

@Composable
context(eventSink: EventSink<InstalledWorldsEvent>)
private fun InstalledWorldList(
    worlds: List<InstalledWorldView>?,
    canQuickJoin: Boolean,
    onAddClick: () -> Unit,
    onOpenFolder: () -> Unit,
    onQuickJoinClick: (String) -> Unit,
    horizontalTabs: Boolean,
) {
    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf(WorldSortBy.LastModified) }
    val listState = rememberLazyListState()
    val importPickerLauncher = rememberFilePickerLauncher(type = FileKitType.File("zip")) { file ->
        file?.let { eventSink(InstalledWorldsEvent.OnImportClick(it.path)) }
    }

    LaunchedEffect(searchQuery, sortBy) { listState.scrollToItem(0) }

    val filteredWorlds = worlds?.filteredAndSorted(searchQuery, sortBy)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .let { if (!horizontalTabs) it.padding(vertical = 8.dp) else it }
            .appCard()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIconTextButton(
                onClick = onAddClick,
                text = stringResource(InstanceRes.string.addWorld),
                imageVector = Lucide.Plus,
                backgroundColor = Theme[colors][primary],
                contentColor = Theme[colors][onPrimary],
            )
            AppSearchField(
                initialValue = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f)
            )
            AppSelectField(
                value = sortBy,
                items = WorldSortBy.entries,
                onValueChange = { sortBy = it },
                itemRenderer = { value, isButton ->
                    AppSelectItem(
                        text = if (isButton) "${stringResource(SharedRes.string.sortBy)}: ${value.displayName}"
                        else value.displayName
                    )
                },
                modifier = Modifier.width(200.dp)
            )
            AppMenuButton {
                AppMenuItem(
                    onClick = { onAddClick() },
                    text = stringResource(InstanceRes.string.addWorld),
                    imageVector = Lucide.Plus
                )
                AppMenuItem(
                    onClick = importPickerLauncher::launch,
                    text = stringResource(InstanceRes.string.importWorld),
                    imageVector = Lucide.FileUp
                )
                AppMenuItem(
                    onClick = onOpenFolder,
                    text = stringResource(InstanceRes.string.openWorldsFolder),
                    imageVector = Lucide.FolderOpen
                )
            }
        }

        when {
            worlds == null -> AppLoading()
            worlds.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AppText(stringResource(InstanceRes.string.noWorldsInstalled))
            }

            filteredWorlds.isNullOrEmpty() -> AppText(stringResource(InstanceRes.string.noMatchingFiles))
            else -> Row(modifier = Modifier.fillMaxSize().padding(end = 4.dp)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(filteredWorlds, key = { it.folderName }) { world ->
                        InstalledWorldRow(world, canQuickJoin, onQuickJoinClick)
                    }
                }
                VerticalScrollbar(adapter = rememberScrollbarAdapter(listState))
            }
        }
    }
}

@Composable
context(_: EventSink<InstalledWorldsEvent>)
private fun InstalledWorldRow(world: InstalledWorldView, canQuickJoin: Boolean, onQuickJoinClick: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (world.iconFilePath != null) {
            Image(
                painter = rememberAsyncImagePainter(model = world.iconFilePath.toPath()),
                contentDescription = world.displayName,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)).background(Theme[colors][surface2]),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(imageVector = Lucide.Earth, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            AppText(
                text = world.displayName,
                fontWeight = FontWeight.SemiBold,
                singleLine = true,
                overflow = TextOverflow.Ellipsis
            )
            world.lastModifiedReadable?.let { lastModifiedReadable ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProvideContentColor(Theme[colors][onBackground].copy(alpha = 0.6f)) {
                        AppIcon(
                            imageVector = Lucide.History,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        AppText(
                            text = lastModifiedReadable,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }

        AppIconTextButton(
            onClick = { onQuickJoinClick(world.folderName) },
            text = stringResource(InstanceRes.string.quickJoin),
            imageVector = Lucide.Play,
            enabled = canQuickJoin,
            backgroundColor = Theme[colors][success],
            contentColor = Theme[colors][onSuccess]
        )
    }
}

private enum class WorldSortBy {
    Name,
    LastModified
}

private val WorldSortBy.displayName: String
    @Composable get() = stringResource(
        when (this) {
            WorldSortBy.Name -> InstanceRes.string.sortByName
            WorldSortBy.LastModified -> InstanceRes.string.sortByLastModified
        }
    )

private fun List<InstalledWorldView>.filteredAndSorted(
    searchQuery: String,
    sortBy: WorldSortBy,
): List<InstalledWorldView> {
    val filtered = filter { searchQuery.isBlank() || it.displayName.contains(searchQuery, ignoreCase = true) }
    return when (sortBy) {
        WorldSortBy.Name -> filtered.sortedBy { it.displayName.lowercase() }
        WorldSortBy.LastModified -> filtered.sortedByDescending { it.lastModified ?: Instant.DISTANT_PAST }
    }
}
