package app.vercors.core.ui.component

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.CoreRes
import app.vercors.core.ui.itemsSelected
import app.vercors.core.ui.modifier.appCard
import app.vercors.core.ui.search
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.onBackground
import app.vercors.core.ui.theme.surface2
import app.vercors.core.ui.theme.surface3
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronUp
import com.composables.icons.lucide.Lucide
import com.composeunstyled.*
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
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.width(IntrinsicSize.Max),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        label?.let {
            AppText(
                text = it,
                fontWeight = FontWeight.Medium
            )
        }

        UnstyledDropdownMenu(
            modifier = Modifier.fillMaxWidth(),
            expanded = expanded,
            onExpandedChange = { expanded = it },
            panel = {
                DropdownMenuPanel(
                    modifier = Modifier.appCard(backgroundColor = Theme[colors][surface2])
                        .width(300.dp) // TODO: find a way to calculate this automatically
                        .heightIn(max = 400.dp),
                ) {
                    if (searchable) {
                        AppTextField(
                            modifier = Modifier.fillMaxWidth(),
                            initialValue = search,
                            placeholder = { AppText(stringResource(CoreRes.string.search)) },
                            onValueChange = onSearchChange,
                            imeAction = ImeAction.Search,
                            backgroundColor = Color.Unspecified,
                            shape = RectangleShape
                        )

                        UnstyledHorizontalSeparator(
                            color = Theme[colors][onBackground]
                        )
                    }

                    MultiSelectOptionList(
                        values = values,
                        items = items,
                        onValueChange = onValueChange,
                        itemRenderer = itemRenderer
                    )
                }
            }
        ) {
            AppButton(
                onClick = { expanded = !expanded },
                backgroundColor = backgroundColor,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    summary(values)
                }
                AppIcon(
                    imageVector = if (expanded) Lucide.ChevronUp else Lucide.ChevronDown,
                    contentDescription = null
                )
            }
        }
    }
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
