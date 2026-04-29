package app.vercors.core.ui.component

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.CoreRes
import app.vercors.core.ui.modifier.appCard
import app.vercors.core.ui.search
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.onBackground
import app.vercors.core.ui.theme.surface
import app.vercors.core.ui.theme.surface2
import com.composables.core.HorizontalSeparator
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronUp
import com.composables.icons.lucide.Lucide
import com.composeunstyled.*
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.stringResource

@Composable
fun <T> AppSelectField(
    value: T,
    items: List<T>,
    onValueChange: (T) -> Unit,
    label: String? = null,
    backgroundColor: Color = Theme[colors][surface],
    itemRenderer: @Composable RowScope.(value: T, isButton: Boolean) -> Unit = { value, _ -> AppSelectItem(value.toString()) },
    searchable: Boolean = false,
    search: String = "",
    onSearchChange: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    anchor: DropdownPanelAnchor = DropdownPanelAnchor.BottomStart
) {
    var expanded by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier) {
        UnstyledDropdownMenu(
            onExpandRequest = { expanded = true },
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                label?.let {
                    AppText(
                        text = it,
                        fontWeight = FontWeight.Medium
                    )
                }

                AppButton(
                    onClick = { expanded = !expanded },
                    backgroundColor = backgroundColor,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemRenderer(value, true)
                    Spacer(Modifier.weight(1f))
                    AppIcon(
                        imageVector = if (expanded) Lucide.ChevronUp else Lucide.ChevronDown,
                        contentDescription = null
                    )
                }
            }
        }

        UnstyledDropdownMenuPanel(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.appCard(backgroundColor = Theme[colors][surface2])
                .widthIn(min = minWidth, max = maxWidth).heightIn(max = 400.dp),
            anchor = anchor
        ) {
            if (searchable) {
                AppTextField(
                    initialValue = search,
                    placeholder = { AppText(stringResource(CoreRes.string.search)) },
                    onValueChange = onSearchChange,
                    imeAction = ImeAction.Search,
                    backgroundColor = Color.Unspecified,
                    shape = RectangleShape
                )

                HorizontalSeparator(
                    color = Theme[colors][onBackground]
                )
            }

            DropdownOptionList(
                value = value,
                items = items,
                onValueChange = { onValueChange(it); expanded = false },
                itemRenderer = itemRenderer
            )
        }
    }
}

@Composable
private fun <T> DropdownOptionList(
    value: T,
    items: List<T>,
    onValueChange: (T) -> Unit,
    itemRenderer: @Composable (RowScope.(T, Boolean) -> Unit)
) {
    Row {
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
                    Row(
                        modifier = Modifier
                            .clickable { onValueChange(it) }
                            .padding(8.dp)
                            .fillMaxWidth(),
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
