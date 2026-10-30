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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.component.*
import app.vercors.core.ui.layout.AppLoading
import app.vercors.core.ui.modifier.appCard
import app.vercors.core.ui.theme.*
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.ui.*
import app.vercors.feature.instance.ui.details.ContentSortBy
import app.vercors.feature.instance.ui.details.InstanceView
import app.vercors.feature.instance.ui.details.displayName
import app.vercors.feature.instance.ui.details.filteredAndSorted
import app.vercors.shared.ui.EventSink
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.sortBy
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
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
fun InstalledWorlds(
    instance: InstanceView,
    content: Store<InstalledWorldsState, InstalledWorldsEvent>,
    horizontalTabs: Boolean,
    onAddClick: () -> Unit,
    onOpenFolder: () -> Unit,
    onQuickJoinClick: (folderName: String) -> Unit
) {
    val canQuickJoin = instance.status is InstanceStatus.Idle || instance.status is InstanceStatus.Failed
    with(content.eventSink) {
        InstalledWorldList(
            worlds = content.state.worlds,
            canQuickJoin = canQuickJoin,
            onAddClick = onAddClick,
            onOpenFolder = onOpenFolder,
            onQuickJoinClick = onQuickJoinClick,
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
    var sortBy by remember { mutableStateOf(ContentSortBy.LastModified) }
    val listState = rememberLazyListState()
    val importPickerLauncher = rememberFilePickerLauncher(type = FileKitType.File("zip")) { file ->
        file?.let { eventSink(InstalledWorldsEvent.OnImportClick(it.path)) }
    }

    LaunchedEffect(searchQuery, sortBy) { listState.scrollToItem(0) }

    val filteredWorlds = worlds?.filteredAndSorted(
        searchQuery,
        sortBy,
        name = { it.displayName },
        lastModified = { it.lastModified },
    )

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
                items = ContentSortBy.entries,
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

// Minecraft rewrites icon.png in place, so the path alone would keep serving Coil's cached image
@Composable
private fun rememberInstalledWorldIconPainter(iconFilePath: String, iconLastModified: Instant?): Painter {
    val cacheKey = "$iconFilePath-$iconLastModified"
    return rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalPlatformContext.current)
            .data(iconFilePath.toPath())
            .memoryCacheKey(cacheKey)
            .diskCacheKey(cacheKey)
            .build()
    )
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
                painter = rememberInstalledWorldIconPainter(world.iconFilePath, world.iconLastModified),
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
                            style = Theme[textStyles][label],
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
