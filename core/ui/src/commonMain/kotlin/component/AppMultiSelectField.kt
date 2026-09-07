package app.vercors.core.ui.component

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.CoreRes
import app.vercors.core.ui.itemsSelected
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.surface2
import app.vercors.core.ui.theme.surface3
import com.composeunstyled.DropdownMenuPanelScope
import com.composeunstyled.UnstyledDropdownMenuItem
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.stringResource

/** Like [AppSelectField], but any number of [items] can be selected via a checkbox instead of picking exactly one. */
@Composable
fun <T> AppMultiSelectField(
    values: Set<T>,
    items: List<T>,
    onValueChange: (T) -> Unit,
    label: String? = null,
    backgroundColor: Color = Theme[colors][surface2],
    itemRenderer: @Composable RowScope.(value: T, isButton: Boolean) -> Unit = { value, _ -> AppSelectItem(value.toString()) },
    summary: @Composable RowScope.(values: Set<T>) -> Unit = { v ->
        AppText(
            stringResource(
                CoreRes.string.itemsSelected,
                v.size
            )
        )
    },
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
        trigger = { summary(values) },
        panel = {
            MultiSelectOptionList(
                values = values,
                items = items,
                onValueChange = onValueChange,
                itemRenderer = itemRenderer
            )
        }
    )
}

@Composable
private fun <T> DropdownMenuPanelScope.MultiSelectOptionList(
    values: Set<T>,
    items: List<T>,
    onValueChange: (T) -> Unit,
    itemRenderer: @Composable (RowScope.(T, Boolean) -> Unit)
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        val listState = rememberLazyListState()

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f)
        ) {
            items.forEach { option ->
                item(option) {
                    UnstyledDropdownMenuItem(
                        onClick = { onValueChange(option) },
                        closeOnClick = false,
                        indication = LocalIndication.current
                    ) {
                        Row(
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand).padding(8.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppCheckbox(
                                checked = option in values,
                                onCheckedChange = { onValueChange(option) },
                                uncheckedBackgroundColor = Theme[colors][surface3]
                            )
                            itemRenderer(option, false)
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
