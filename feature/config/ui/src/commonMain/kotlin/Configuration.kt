package app.vercors.feature.config.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import app.vercors.core.ui.animation.appAnimateItem
import app.vercors.core.ui.component.*
import app.vercors.core.ui.layout.AppSidebarTabItem
import app.vercors.core.ui.layout.AppTabGroup
import app.vercors.core.ui.modifier.appCard
import app.vercors.core.ui.modifier.clickableText
import app.vercors.core.ui.theme.*
import app.vercors.feature.config.domain.model.HomeCategoryConfig
import app.vercors.feature.config.domain.model.StartupScreen
import app.vercors.shared.domain.model.DEFAULT_MIN_MEMORY_MB
import app.vercors.shared.domain.model.JavaInstallation
import app.vercors.shared.domain.model.defaultMaxMemoryMb
import app.vercors.shared.ui.*
import app.vercors.shared.ui.component.LibraryPathInputs
import app.vercors.shared.ui.component.MemorySliderInputs
import app.vercors.shared.ui.overlay.ConfirmDialog
import app.vercors.shared.ui.overlay.ConfirmDialogButton
import app.vercors.shared.ui.overlay.ConfirmDialogButtonStyle
import com.composables.icons.lucide.*
import com.composeunstyled.theme.Theme
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.overlay.OverlayEffect
import dev.zacsweers.metro.AppScope
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.path
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@CircuitInject(ConfigurationScreen::class, AppScope::class)
@Composable
fun Configuration(
    store: Store<ConfigurationState, ConfigurationEvent>,
    modifier: Modifier = Modifier,
) {
    val (state, eventSink) = store
    with(eventSink) {
        if (state.promptClearContentCache) {
            OverlayEffect {
                val result = show(
                    ConfirmDialog(
                        messageResource = ConfigRes.string.clearCacheConfirmation,
                        actionButton = ConfirmDialogButton(
                            ConfigRes.string.clearCache,
                            Lucide.Trash,
                            ConfirmDialogButtonStyle.Destructive
                        ),
                    )
                )
                eventSink(ConfigurationEvent.OnConfirmClearContentCache(result))
            }
        }

        if (state.promptChangeDataDirectory) {
            OverlayEffect {
                show(state.changeDataDirectoryDialog)
                eventSink(ConfigurationEvent.OnChangeDataDirectoryDismiss)
            }
        }

        AppTabGroup(
            selectedTab = state.selectedTab,
            onSelectedTabChange = { eventSink(ConfigurationEvent.OnTabClick(it)) },
            tabs = ConfigurationTab.entries.filter { it != ConfigurationTab.NativeLibraries || state.showNativeLibraryOptions },
            modifier = modifier.fillMaxSize(),
            orientation = if (state.horizontalTabs == true) Orientation.Horizontal else Orientation.Vertical,
            tabTitle = { tab -> AppSidebarTabItem(title = tab.title, icon = tab.icon) }
        ) { tab -> ConfigurationTabContent(tab, state) }
    }
}

@Composable
context(eventSink: EventSink<ConfigurationEvent>)
private fun ConfigurationTabContent(tab: ConfigurationTab, state: ConfigurationState) {
    when (tab) {
        ConfigurationTab.General -> GeneralConfigurationTab(state)
        ConfigurationTab.Advanced -> AdvancedConfigurationTab(state)
        ConfigurationTab.UI -> UIConfigurationTab(state)
        ConfigurationTab.Java -> JavaConfigurationTab(state)
        ConfigurationTab.NativeLibraries -> NativeLibraryConfigurationTab(state)
        ConfigurationTab.Storage -> StorageConfigurationTab(state)
    }
}

@Composable
context(eventSink: EventSink<ConfigurationEvent>)
private fun GeneralConfigurationTab(
    state: ConfigurationState
) {
    ConfigurationTabScaffold(state.horizontalTabs) {
        if (state.platformList.isNotEmpty()) {
            ConfigurationRow(
                title = ConfigRes.string.preferredPlatform,
                description = ConfigRes.string.preferredPlatformDesc,
            ) {
                AppSelectField(
                    value = state.platformList.find { it.id == state.preferredPlatform } ?: state.platformList.first(),
                    items = state.platformList,
                    onValueChange = { eventSink(ConfigurationEvent.OnPreferredPlatformChange(it.id)) },
                    itemRenderer = { platform, _ ->
                        AppText(text = platform.name)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (state.startupScreen != null) {
            ConfigurationRow(
                title = ConfigRes.string.startupScreen,
                description = ConfigRes.string.startupScreenDesc,
            ) {
                AppSelectField(
                    value = state.startupScreen,
                    items = StartupScreen.entries,
                    onValueChange = { eventSink(ConfigurationEvent.OnStartupScreenChange(it)) },
                    itemRenderer = { startupScreen, _ ->
                        AppText(text = stringResource(startupScreen.label))
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                AppText(text = stringResource(ConfigRes.string.homeSections), fontWeight = FontWeight.SemiBold)
                AppText(text = stringResource(ConfigRes.string.homeSectionsDesc), style = Theme[textStyles][label])
            }
            HomeCategoriesReorderList(homeCategories = state.homeCategories)
        }
    }
}

@Composable
context(eventSink: EventSink<ConfigurationEvent>)
private fun AdvancedConfigurationTab(
    state: ConfigurationState
) {
    ConfigurationTabScaffold(state.horizontalTabs) {
        ConfigurationRow(
            title = ConfigRes.string.metaApiUrl,
            description = ConfigRes.string.metaApiUrlDesc,
            requiresRestart = true,
        ) {
            AppTextField(
                initialValue = state.metaApiUrl.orEmpty(),
                onValueChange = { eventSink(ConfigurationEvent.OnMetaApiUrlChange(it)) },
                debounceMillis = 500,
                backgroundColor = Theme[colors][surface2],
                modifier = Modifier.weight(1f)
            )
        }
        if (state.networkTimeoutSeconds != null) {
            ConfigurationRow(
                title = ConfigRes.string.networkTimeout,
                description = ConfigRes.string.networkTimeoutDesc,
                requiresRestart = true,
            ) {
                AppNumberField(
                    value = state.networkTimeoutSeconds,
                    onValueChange = { eventSink(ConfigurationEvent.OnNetworkTimeoutChange(it)) },
                    valueRange = 1..120,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (state.parallelDownloads != null) {
            ConfigurationRow(
                title = ConfigRes.string.parallelDownloads,
                description = ConfigRes.string.parallelDownloadsDesc,
            ) {
                AppNumberField(
                    value = state.parallelDownloads,
                    onValueChange = { eventSink(ConfigurationEvent.OnParallelDownloadsChange(it)) },
                    valueRange = 1..64,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (state.parallelFilesystemOperations != null) {
            ConfigurationRow(
                title = ConfigRes.string.parallelFilesystemOperations,
                description = ConfigRes.string.parallelFilesystemOperationsDesc,
            ) {
                AppNumberField(
                    value = state.parallelFilesystemOperations,
                    onValueChange = { eventSink(ConfigurationEvent.OnParallelFilesystemOperationsChange(it)) },
                    valueRange = 1..64,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private val HOME_CATEGORY_ROW_HEIGHT = 32.dp
private val UI_SCALE_OPTIONS = listOf(70, 80, 90, 100, 110, 125, 150, 175, 200)

@Composable
context(eventSink: EventSink<ConfigurationEvent>)
private fun HomeCategoriesReorderList(homeCategories: List<HomeCategoryConfig>) {
    val uncheckedCategories = remember(homeCategories) {
        val checkedSet = homeCategories.toSet()
        HomeCategoryConfig.entries.filterNot { it in checkedSet }
    }
    val orderState = remember(homeCategories) { mutableStateOf(homeCategories) }
    val draggedIndexState = remember { mutableStateOf<Int?>(null) }
    val dragOffsetState = remember { mutableStateOf(0f) }
    val rowHeightPx = with(LocalDensity.current) { HOME_CATEGORY_ROW_HEIGHT.toPx() }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().height((HOME_CATEGORY_ROW_HEIGHT * HomeCategoryConfig.entries.size)),
    ) {
        itemsIndexed(orderState.value, key = { _, it -> it }) { index, it ->
            CheckedHomeCategoryRow(
                category = it,
                index = index,
                rowHeightPx = rowHeightPx,
                orderState = orderState,
                draggedIndexState = draggedIndexState,
                dragOffsetState = dragOffsetState,
            )
        }
        items(uncheckedCategories, key = { it }) { category ->
            UncheckedHomeCategoryRow(category = category, homeCategories = homeCategories)
        }
    }
}

@Composable
context(eventSink: EventSink<ConfigurationEvent>)
private fun LazyItemScope.CheckedHomeCategoryRow(
    category: HomeCategoryConfig,
    index: Int,
    rowHeightPx: Float,
    orderState: MutableState<List<HomeCategoryConfig>>,
    draggedIndexState: MutableState<Int?>,
    dragOffsetState: MutableState<Float>,
) {
    val order = orderState.value
    val isDragged = index == draggedIndexState.value
    val latestIndex = rememberUpdatedState(index)
    Row(
        modifier = Modifier.fillMaxWidth()
            .height(HOME_CATEGORY_ROW_HEIGHT)
            .let { if (isDragged) it else it.appAnimateItem() }
            .zIndex(if (isDragged) 1f else 0f)
            .offset { IntOffset(0, if (isDragged) dragOffsetState.value.roundToInt() else 0) },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HomeCategoryReorderArrow(
            imageVector = Lucide.ChevronUp,
            enabled = index > 0,
            onClick = { eventSink(ConfigurationEvent.OnHomeCategoriesChange(order.moved(index, index - 1))) },
        )
        HomeCategoryReorderArrow(
            imageVector = Lucide.ChevronDown,
            enabled = index < order.lastIndex,
            onClick = { eventSink(ConfigurationEvent.OnHomeCategoriesChange(order.moved(index, index + 1))) },
        )
        AppIcon(
            imageVector = Lucide.GripVertical,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
                .pointerHoverIcon(icon = PointerIcon.Hand)
                .homeCategoryDragHandle(latestIndex, rowHeightPx, orderState, draggedIndexState, dragOffsetState) {
                    eventSink(ConfigurationEvent.OnHomeCategoriesChange(orderState.value))
                }
        )
        AppCheckbox(
            checked = true,
            onCheckedChange = { eventSink(ConfigurationEvent.OnHomeCategoriesChange(order - category)) }
        )
        AppText(text = stringResource(category.label), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun HomeCategoryReorderArrow(imageVector: ImageVector, enabled: Boolean, onClick: () -> Unit) {
    AppIcon(
        imageVector = imageVector,
        contentDescription = null,
        modifier = Modifier.size(16.dp).let { if (enabled) it.clickableText(onClick) else it.alpha(0.3f) }
    )
}

private fun Modifier.homeCategoryDragHandle(
    index: State<Int>,
    rowHeightPx: Float,
    orderState: MutableState<List<HomeCategoryConfig>>,
    draggedIndexState: MutableState<Int?>,
    dragOffsetState: MutableState<Float>,
    onDragFinished: () -> Unit,
): Modifier = pointerInput(Unit) {
    detectDragGestures(
        onDragStart = {
            draggedIndexState.value = index.value
            dragOffsetState.value = 0f
        },
        onDragEnd = {
            draggedIndexState.value = null
            dragOffsetState.value = 0f
            onDragFinished()
        },
        onDragCancel = {
            draggedIndexState.value = null
            dragOffsetState.value = 0f
        },
        onDrag = { change, dragAmount ->
            change.consume()
            val currentIndex = draggedIndexState.value ?: return@detectDragGestures
            val order = orderState.value
            dragOffsetState.value += dragAmount.y
            val targetIndex = (currentIndex + (dragOffsetState.value / rowHeightPx).roundToInt())
                .coerceIn(0, order.lastIndex)
            if (targetIndex != currentIndex) {
                orderState.value = order.moved(currentIndex, targetIndex)
                dragOffsetState.value -= (targetIndex - currentIndex) * rowHeightPx
                draggedIndexState.value = targetIndex
            }
        }
    )
}

@Composable
context(eventSink: EventSink<ConfigurationEvent>)
private fun LazyItemScope.UncheckedHomeCategoryRow(
    category: HomeCategoryConfig,
    homeCategories: List<HomeCategoryConfig>
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(HOME_CATEGORY_ROW_HEIGHT).appAnimateItem(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.size(64.dp))
        AppCheckbox(
            checked = false,
            onCheckedChange = { eventSink(ConfigurationEvent.OnHomeCategoriesChange(homeCategories + category)) }
        )
        AppText(
            text = stringResource(category.label),
            modifier = Modifier.weight(1f),
            color = Theme[colors][onBackground].copy(alpha = 0.6f)
        )
    }
}

@Composable
context(eventSink: EventSink<ConfigurationEvent>)
private fun UIConfigurationTab(
    state: ConfigurationState
) {
    ConfigurationTabScaffold(state.horizontalTabs) {
        if (state.selectedTheme != null) {
            ConfigurationRow(
                title = ConfigRes.string.theme,
                description = ConfigRes.string.themeDesc,
            ) {
                AppSelectField(
                    value = state.selectedTheme,
                    items = state.themeList,
                    onValueChange = { eventSink(ConfigurationEvent.OnThemeChange(it.id)) },
                    itemRenderer = { theme, _ ->
                        AppText(text = theme.displayName)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            if (state.selectedAccent != null) {
                ConfigurationRow(
                    title = ConfigRes.string.accent,
                    description = ConfigRes.string.accentDesc,
                ) {
                    AppSelectField(
                        value = state.selectedAccent,
                        items = state.selectedTheme.accents,
                        onValueChange = { eventSink(ConfigurationEvent.OnAccentChange(it.id)) },
                        itemRenderer = { accent, _ ->
                            Box(modifier = Modifier.size(16.dp).background(accent.color))
                            AppText(text = accent.name)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        if (state.animations != null) {
            ConfigurationRow(
                title = ConfigRes.string.animations,
                description = ConfigRes.string.animationsDesc,
            ) {
                AppCheckbox(
                    checked = state.animations,
                    onCheckedChange = { eventSink(ConfigurationEvent.OnAnimationsToggle) }
                )
            }
        }
        if (state.uiScalePercent != null) {
            ConfigurationRow(
                title = ConfigRes.string.uiScale,
                description = ConfigRes.string.uiScaleDesc,
            ) {
                AppSelectField(
                    value = state.uiScalePercent,
                    items = UI_SCALE_OPTIONS,
                    onValueChange = { eventSink(ConfigurationEvent.OnUiScalePercentChange(it)) },
                    itemRenderer = { percent, _ -> AppSelectItem(text = "$percent%") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (state.horizontalTabs != null) {
            ConfigurationRow(
                title = ConfigRes.string.horizontalTabs,
                description = ConfigRes.string.horizontalTabsDesc,
            ) {
                AppCheckbox(
                    checked = state.horizontalTabs,
                    onCheckedChange = { eventSink(ConfigurationEvent.OnHorizontalTabsToggle) }
                )
            }
        }
    }
}

@Composable
context(eventSink: EventSink<ConfigurationEvent>)
private fun JavaConfigurationTab(
    state: ConfigurationState
) {
    ConfigurationTabScaffold(state.horizontalTabs) {
        val pickerLauncher = rememberDirectoryPickerLauncher { directory ->
            directory?.let { eventSink(ConfigurationEvent.OnJavaInstallationSelected(it.path)) }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                AppText(text = stringResource(ConfigRes.string.javaInstallations), fontWeight = FontWeight.SemiBold)
                AppText(text = stringResource(ConfigRes.string.javaInstallationsDesc), style = Theme[textStyles][label])
            }
            AppIconTextButton(
                onClick = pickerLauncher::launch,
                text = stringResource(ConfigRes.string.addInstallation),
                imageVector = Lucide.Plus,
            )
            AppIconTextButton(
                onClick = { eventSink(ConfigurationEvent.OnRescanJavaClick) },
                text = stringResource(if (state.javaScanInProgress) ConfigRes.string.rescanning else ConfigRes.string.rescan),
                imageVector = Lucide.RefreshCw,
                enabled = !state.javaScanInProgress,
            )
        }

        state.javaInstallations.groupBy { it.majorVersion }.toSortedMap().forEach { (majorVersion, installations) ->
            JavaVersionGroup(majorVersion, installations, state.preferredJavaByMajorVersion[majorVersion])
        }

        state.javaAddError?.let { path ->
            AppText(
                text = stringResource(ConfigRes.string.javaAddFailed, path),
                color = Theme[colors][error],
                modifier = Modifier.padding(horizontal = 8.dp)
                    .clickable { eventSink(ConfigurationEvent.OnJavaAddErrorDismiss) }
            )
        }

        val defaultMax = defaultMaxMemoryMb(state.totalMemoryMb)

        ConfigurationRow(title = ConfigRes.string.maxMemory, description = ConfigRes.string.maxMemoryDesc) {
            MemorySliderControl(
                value = state.jvmMemoryOptions.maxMemoryMb,
                defaultValue = defaultMax,
                valueRange = 512..state.totalMemoryMb,
                stepSize = 128,
                onCheckedChange = { checked -> eventSink(ConfigurationEvent.OnMaxMemoryMbChange(if (checked) defaultMax else null)) },
                onValueChange = { eventSink(ConfigurationEvent.OnMaxMemoryMbChange(it)) },
            )
        }
        ConfigurationRow(title = ConfigRes.string.minMemory, description = ConfigRes.string.minMemoryDesc) {
            MemorySliderControl(
                value = state.jvmMemoryOptions.minMemoryMb,
                defaultValue = DEFAULT_MIN_MEMORY_MB,
                valueRange = 256..(state.jvmMemoryOptions.maxMemoryMb ?: defaultMax),
                stepSize = 32,
                onCheckedChange = { checked -> eventSink(ConfigurationEvent.OnMinMemoryMbChange(if (checked) DEFAULT_MIN_MEMORY_MB else null)) },
                onValueChange = { eventSink(ConfigurationEvent.OnMinMemoryMbChange(it)) },
            )
        }
        ConfigurationRow(title = ConfigRes.string.extraJvmArgs, description = ConfigRes.string.extraJvmArgsDesc) {
            AppTextField(
                initialValue = state.jvmMemoryOptions.extraArgs.orEmpty(),
                onValueChange = { eventSink(ConfigurationEvent.OnExtraJvmArgsChange(it)) },
                debounceMillis = 500,
                backgroundColor = Theme[colors][surface2],
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
context(eventSink: EventSink<ConfigurationEvent>)
private fun NativeLibraryConfigurationTab(
    state: ConfigurationState
) {
    ConfigurationTabScaffold(state.horizontalTabs) {
        ConfigurationRow(title = ConfigRes.string.useSystemGlfw, description = ConfigRes.string.useSystemGlfwDesc) {
            LibraryOverrideRow(
                checked = state.nativeLibraryOptions.useSystemGlfw == true,
                path = state.nativeLibraryOptions.glfwPath.orEmpty(),
                detectedPath = state.detectedGlfwPath,
                onCheckedChange = { eventSink(ConfigurationEvent.OnUseSystemGlfwToggle(it)) },
                onPathChange = { eventSink(ConfigurationEvent.OnGlfwPathChange(it)) },
            )
        }
        ConfigurationRow(title = ConfigRes.string.useSystemOpenAl, description = ConfigRes.string.useSystemOpenAlDesc) {
            LibraryOverrideRow(
                checked = state.nativeLibraryOptions.useSystemOpenAl == true,
                path = state.nativeLibraryOptions.openAlPath.orEmpty(),
                detectedPath = state.detectedOpenAlPath,
                onCheckedChange = { eventSink(ConfigurationEvent.OnUseSystemOpenAlToggle(it)) },
                onPathChange = { eventSink(ConfigurationEvent.OnOpenAlPathChange(it)) },
            )
        }
    }
}

@Composable
context(eventSink: EventSink<ConfigurationEvent>)
private fun StorageConfigurationTab(
    state: ConfigurationState
) {
    ConfigurationTabScaffold(state.horizontalTabs) {
        ConfigurationRow(
            title = ConfigRes.string.dataDirectory,
            description = ConfigRes.string.dataDirectoryDesc,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppText(
                    text = state.dataDirectory,
                    style = Theme[textStyles][label],
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                AppIconTextButton(
                    onClick = { eventSink(ConfigurationEvent.OnOpenDataDirectoryClick) },
                    text = stringResource(SharedRes.string.open),
                    imageVector = Lucide.FolderOpen,
                )
                AppIconTextButton(
                    onClick = { eventSink(ConfigurationEvent.OnChangeDataDirectoryClick) },
                    text = stringResource(ConfigRes.string.changeDataDirectory),
                    imageVector = Lucide.FolderInput,
                    enabled = state.canChangeDataDirectory,
                )
            }
        }
        ConfigurationRow(
            title = ConfigRes.string.contentCache,
            description = ConfigRes.string.contentCacheDesc,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                state.contentCacheSizeBytes?.let {
                    AppText(text = it.toHumanReadableSize())
                }
                AppIconTextButton(
                    onClick = { eventSink(ConfigurationEvent.OnClearContentCacheClick) },
                    text = stringResource(ConfigRes.string.clearCache),
                    imageVector = Lucide.Trash,
                    backgroundColor = Theme[colors][error],
                    contentColor = Theme[colors][onError]
                )
            }
        }
    }
}

@Composable
private fun RowScope.LibraryOverrideRow(
    checked: Boolean,
    path: String,
    detectedPath: String?,
    onCheckedChange: (Boolean) -> Unit,
    onPathChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier.weight(1f),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LibraryPathInputs(checked, path, detectedPath, onCheckedChange, onPathChange)
    }
}

@Composable
private fun RowScope.MemorySliderControl(
    value: Int?,
    defaultValue: Int,
    valueRange: IntRange,
    stepSize: Int,
    onCheckedChange: (Boolean) -> Unit,
    onValueChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.weight(1f),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MemorySliderInputs(
            checked = value != null,
            value = value ?: defaultValue,
            valueRange = valueRange,
            stepSize = stepSize,
            onCheckedChange = onCheckedChange,
            onValueChange = onValueChange,
        )
    }
}

@Composable
context(eventSink: EventSink<ConfigurationEvent>)
private fun JavaVersionGroup(majorVersion: Int, installations: List<JavaInstallation>, preferredId: String?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(
                imageVector = Lucide.Dot,
                contentDescription = null
            )
            AppText(
                text = stringResource(ConfigRes.string.javaMajorVersion, majorVersion),
                fontWeight = FontWeight.SemiBold
            )
        }
        AppRadioGroup(
            value = preferredId,
            onValueChange = { id -> eventSink(ConfigurationEvent.OnPreferredJavaChange(majorVersion, id)) },
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                installations.forEach { installation ->
                    JavaInstallationRow(installation = installation, isSelected = installation.id == preferredId)
                }
            }
        }
    }
}

@Composable
private fun AppRadioGroupScope.JavaInstallationRow(installation: JavaInstallation, isSelected: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            AppText(
                text = "${installation.versionString} (${installation.vendor}, ${installation.arch})",
                fontSize = 12.sp
            )
            AppText(
                text = installation.path,
                style = Theme[textStyles][label],
                color = Theme[colors][onBackground].copy(alpha = 0.6f)
            )
        }
        AppRadioButton(
            value = installation.id,
            label = if (isSelected) stringResource(ConfigRes.string.preferred) else null,
        )
    }
}

@Composable
private fun ConfigurationTabScaffold(
    horizontalTabs: Boolean?,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Column(
            modifier = Modifier.let { if (horizontalTabs == false) it.padding(vertical = 8.dp) else it }.appCard()
                .padding(8.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun ConfigurationRow(
    title: StringResource,
    description: StringResource,
    requiresRestart: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            AppText(text = stringResource(title), fontWeight = FontWeight.SemiBold)
            AppText(text = stringResource(description), style = Theme[textStyles][label])
            if (requiresRestart) {
                AppText(
                    text = stringResource(ConfigRes.string.restartRequired),
                    style = Theme[textStyles][label],
                    color = Theme[colors][onBackground].copy(alpha = 0.6f)
                )
            }
        }
        content()
    }
}

private fun <T> List<T>.moved(from: Int, to: Int): List<T> =
    toMutableList().apply { add(to, removeAt(from)) }

private fun Long.toHumanReadableSize(): String {
    if (this < 1024) return "$this B"
    val units = listOf("KB", "MB", "GB", "TB")
    var value = this / 1024.0
    var unitIndex = 0
    while (value >= 1024 && unitIndex < units.lastIndex) {
        value /= 1024
        unitIndex++
    }
    return "${(value * 10).toInt() / 10.0} ${units[unitIndex]}"
}


private val ConfigurationTab.icon: ImageVector
    @Stable get() = when (this) {
        ConfigurationTab.General -> Lucide.Settings
        ConfigurationTab.UI -> Lucide.Eye
        ConfigurationTab.Java -> Lucide.Coffee
        ConfigurationTab.NativeLibraries -> Lucide.Package
        ConfigurationTab.Storage -> Lucide.HardDrive
        ConfigurationTab.Advanced -> Lucide.Wrench
    }

private val ConfigurationTab.title: String
    @Composable get() = when (this) {
        ConfigurationTab.General -> stringResource(ConfigRes.string.general)
        ConfigurationTab.UI -> stringResource(ConfigRes.string.ui)
        ConfigurationTab.Java -> stringResource(ConfigRes.string.java)
        ConfigurationTab.NativeLibraries -> stringResource(ConfigRes.string.nativeLibraries)
        ConfigurationTab.Storage -> stringResource(ConfigRes.string.storage)
        ConfigurationTab.Advanced -> stringResource(ConfigRes.string.advanced)
    }

private val StartupScreen.label: StringResource
    get() = when (this) {
        StartupScreen.Home -> SharedRes.string.home
        StartupScreen.Instances -> SharedRes.string.instances
        StartupScreen.Content -> SharedRes.string.content
        StartupScreen.Settings -> SharedRes.string.settings
        StartupScreen.Accounts -> SharedRes.string.account
        StartupScreen.LastPlayedInstance -> ConfigRes.string.startupScreenLastPlayedInstance
    }

private val HomeCategoryConfig.label: StringResource
    get() = when (this) {
        HomeCategoryConfig.Instances -> SharedRes.string.instances
        HomeCategoryConfig.Modpacks -> ConfigRes.string.homeCategoryModpacks
        HomeCategoryConfig.Mods -> SharedRes.string.mods
        HomeCategoryConfig.ResourcePacks -> SharedRes.string.resourcePacks
        HomeCategoryConfig.ShaderPacks -> SharedRes.string.shaderPacks
    }
