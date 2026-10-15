package app.vercors.feature.config.ui.general

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import app.vercors.core.ui.animation.appAnimateItem
import app.vercors.core.ui.component.*
import app.vercors.core.ui.modifier.clickableText
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.label
import app.vercors.core.ui.theme.onBackground
import app.vercors.core.ui.theme.textStyles
import app.vercors.feature.config.domain.model.HomeCategoryConfig
import app.vercors.feature.config.domain.model.StartupScreen
import app.vercors.feature.config.ui.*
import app.vercors.feature.project.ui.icon
import app.vercors.shared.ui.*
import com.composables.icons.lucide.*
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
fun GeneralConfiguration(
    content: Store<GeneralConfigurationState, GeneralConfigurationEvent>,
    horizontalTabs: Boolean?,
) {
    val (state, eventSink) = content
    with(eventSink) {
        GeneralConfigurationContent(state, horizontalTabs)
    }
}

@Composable
context(eventSink: EventSink<GeneralConfigurationEvent>)
private fun GeneralConfigurationContent(state: GeneralConfigurationState, horizontalTabs: Boolean?) {
    ConfigurationTabScaffold(horizontalTabs) {
        if (state.platformList.isNotEmpty()) {
            ConfigurationRow(
                title = ConfigRes.string.preferredPlatform,
                description = ConfigRes.string.preferredPlatformDesc,
            ) {
                AppSelectField(
                    value = state.platformList.find { it.id == state.preferredPlatform } ?: state.platformList.first(),
                    items = state.platformList,
                    onValueChange = { eventSink(GeneralConfigurationEvent.OnPreferredPlatformChange(it.id)) },
                    itemRenderer = { platform, _ ->
                        AppSelectItem(text = platform.name, icon = platform.icon)
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
                    onValueChange = { eventSink(GeneralConfigurationEvent.OnStartupScreenChange(it)) },
                    itemRenderer = { startupScreen, _ ->
                        AppSelectItem(text = stringResource(startupScreen.label), icon = startupScreen.icon)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (state.checkForInstanceUpdates != null) {
            ConfigurationRow(
                title = ConfigRes.string.checkForInstanceUpdates,
                description = ConfigRes.string.checkForInstanceUpdatesDesc,
            ) {
                AppCheckbox(
                    checked = state.checkForInstanceUpdates,
                    onCheckedChange = { eventSink(GeneralConfigurationEvent.OnCheckForInstanceUpdatesToggle) }
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
            GeneralConfigurationHomeCategories(homeCategories = state.homeCategories)
        }
    }
}

private val HOME_CATEGORY_ROW_HEIGHT = 32.dp

@Composable
context(eventSink: EventSink<GeneralConfigurationEvent>)
private fun GeneralConfigurationHomeCategories(homeCategories: List<HomeCategoryConfig>) {
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
            GeneralConfigurationCheckedHomeCategory(
                category = it,
                index = index,
                rowHeightPx = rowHeightPx,
                orderState = orderState,
                draggedIndexState = draggedIndexState,
                dragOffsetState = dragOffsetState,
            )
        }
        items(uncheckedCategories, key = { it }) { category ->
            GeneralConfigurationUncheckedHomeCategory(category = category, homeCategories = homeCategories)
        }
    }
}

@Composable
context(eventSink: EventSink<GeneralConfigurationEvent>)
private fun LazyItemScope.GeneralConfigurationCheckedHomeCategory(
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
        GeneralConfigurationReorderArrow(
            imageVector = Lucide.ChevronUp,
            enabled = index > 0,
            onClick = { eventSink(GeneralConfigurationEvent.OnHomeCategoriesChange(order.moved(index, index - 1))) },
        )
        GeneralConfigurationReorderArrow(
            imageVector = Lucide.ChevronDown,
            enabled = index < order.lastIndex,
            onClick = { eventSink(GeneralConfigurationEvent.OnHomeCategoriesChange(order.moved(index, index + 1))) },
        )
        AppIcon(
            imageVector = Lucide.GripVertical,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
                .pointerHoverIcon(icon = PointerIcon.Hand)
                .homeCategoryDragHandle(latestIndex, rowHeightPx, orderState, draggedIndexState, dragOffsetState) {
                    eventSink(GeneralConfigurationEvent.OnHomeCategoriesChange(orderState.value))
                }
        )
        AppCheckbox(
            checked = true,
            onCheckedChange = { eventSink(GeneralConfigurationEvent.OnHomeCategoriesChange(order - category)) }
        )
        AppText(text = stringResource(category.label), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun GeneralConfigurationReorderArrow(imageVector: ImageVector, enabled: Boolean, onClick: () -> Unit) {
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
context(eventSink: EventSink<GeneralConfigurationEvent>)
private fun LazyItemScope.GeneralConfigurationUncheckedHomeCategory(
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
            onCheckedChange = { eventSink(GeneralConfigurationEvent.OnHomeCategoriesChange(homeCategories + category)) }
        )
        AppText(
            text = stringResource(category.label),
            modifier = Modifier.weight(1f),
            color = Theme[colors][onBackground].copy(alpha = 0.6f)
        )
    }
}

private fun <T> List<T>.moved(from: Int, to: Int): List<T> =
    toMutableList().apply { add(to, removeAt(from)) }

private val StartupScreen.label: StringResource
    get() = when (this) {
        StartupScreen.Home -> SharedRes.string.home
        StartupScreen.Instances -> SharedRes.string.instances
        StartupScreen.Content -> SharedRes.string.content
        StartupScreen.Settings -> SharedRes.string.settings
        StartupScreen.Accounts -> SharedRes.string.account
        StartupScreen.LastPlayedInstance -> ConfigRes.string.startupScreenLastPlayedInstance
    }

private val StartupScreen.icon: ImageVector
    get() = when (this) {
        StartupScreen.Home -> Lucide.House
        StartupScreen.Instances -> Lucide.Gamepad2
        StartupScreen.Content -> Lucide.Box
        StartupScreen.Settings -> Lucide.Settings
        StartupScreen.Accounts -> Lucide.User
        StartupScreen.LastPlayedInstance -> Lucide.History
    }

private val HomeCategoryConfig.label: StringResource
    get() = when (this) {
        HomeCategoryConfig.Instances -> SharedRes.string.instances
        HomeCategoryConfig.Modpacks -> ConfigRes.string.homeCategoryModpacks
        HomeCategoryConfig.Mods -> SharedRes.string.mods
        HomeCategoryConfig.ResourcePacks -> SharedRes.string.resourcePacks
        HomeCategoryConfig.ShaderPacks -> SharedRes.string.shaderPacks
    }
