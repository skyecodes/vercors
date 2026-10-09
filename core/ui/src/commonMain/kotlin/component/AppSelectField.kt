package app.vercors.core.ui.component

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.surface2
import com.composeunstyled.DropdownMenuPanelScope
import com.composeunstyled.LocalTextStyle
import com.composeunstyled.ProvideTextStyle
import com.composeunstyled.UnstyledDropdownMenuItem
import com.composeunstyled.theme.Theme

@Composable
fun <T> AppSelectField(
    value: T,
    items: List<T>,
    onValueChange: (T) -> Unit,
    label: String? = null,
    backgroundColor: Color = Theme[colors][surface2],
    itemRenderer: @Composable RowScope.(value: T, isButton: Boolean) -> Unit = { value, _ -> AppSelectItem(value.toString()) },
    searchable: Boolean = false,
    search: String = "",
    onSearchChange: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    SelectFieldScaffold(
        label = label,
        backgroundColor = backgroundColor,
        searchable = searchable,
        search = search,
        onSearchChange = onSearchChange,
        modifier = modifier,
        trigger = { itemRenderer(value, true) },
        panel = { closeMenu ->
            DropdownOptionList(
                value = value,
                items = items,
                onValueChange = { onValueChange(it); closeMenu() },
                itemRenderer = itemRenderer
            )
        }
    )
}

@Composable
fun <T> AppLabeledSelectField(
    label: String,
    value: T,
    items: List<T>,
    onValueChange: (T) -> Unit,
    displayName: @Composable (T) -> String,
    badgeIcon: @Composable (T) -> ImageVector? = { null },
    backgroundColor: Color = Theme[colors][surface2],
    searchable: Boolean = false,
    search: String = "",
    onSearchChange: (String) -> Unit = {},
    modifier: Modifier = Modifier.width(300.dp),
) {
    AppSelectField(
        label = label,
        value = value,
        items = items,
        onValueChange = onValueChange,
        itemRenderer = { v, _ -> AppSelectItem(text = displayName(v), icon = badgeIcon(v)) },
        searchable = searchable,
        search = search,
        onSearchChange = onSearchChange,
        backgroundColor = backgroundColor,
        modifier = modifier,
    )
}

@Composable
private fun <T> DropdownMenuPanelScope.DropdownOptionList(
    value: T,
    items: List<T>,
    onValueChange: (T) -> Unit,
    itemRenderer: @Composable (RowScope.(T, Boolean) -> Unit)
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        val listState = rememberLazyListState()

        LaunchedEffect(value) {
            val index = items.indexOfFirst { it == value } - (listState.layoutInfo.visibleItemsInfo.size - 1) / 2
            if (index >= 0) listState.scrollToItem(index)
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f)
        ) {
            items.forEach {
                item(it) {
                    UnstyledDropdownMenuItem(
                        onClick = { onValueChange(it) },
                        indication = LocalIndication.current
                    ) {
                        Row(
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand).padding(8.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProvideTextStyle(
                                LocalTextStyle.current.copy(
                                    fontWeight = if (it == value) FontWeight.SemiBold else FontWeight.Normal
                                )
                            ) {
                                itemRenderer(it, false)
                            }
                        }
                    }
                }
            }
        }

        if (listState.canScrollBackward || listState.canScrollForward) {
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(listState)
            )
        }
    }
}

@Composable
fun RowScope.AppSelectItem(
    text: String,
    icon: ImageVector? = null
) {
    icon?.let {
        AppIcon(
            imageVector = it,
            contentDescription = text,
            modifier = Modifier.size(16.dp)
        )
    }
    AppText(text)
}
