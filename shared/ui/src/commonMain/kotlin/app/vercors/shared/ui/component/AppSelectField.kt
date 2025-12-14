package app.vercors.shared.ui.component

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.vercors.shared.ui.colors
import app.vercors.shared.ui.modifier.appCard
import app.vercors.shared.ui.onBackground
import app.vercors.shared.ui.res.Res
import app.vercors.shared.ui.res.search
import app.vercors.shared.ui.surface
import app.vercors.shared.ui.surface2
import com.composables.core.HorizontalSeparator
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.Lucide
import com.composeunstyled.DropdownMenu
import com.composeunstyled.DropdownMenuPanel
import com.composeunstyled.DropdownPanelAnchor
import com.composeunstyled.Text
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.stringResource

@Composable
fun <T> AppSelectField(
    label: String,
    value: T,
    items: List<T>,
    onValueChange: (T) -> Unit,
    backgroundColor: Color = Theme[colors][surface],
    textConverter: @Composable (T) -> String = { it.toString() },
    itemRenderer: @Composable RowScope.(T) -> Unit = {
        Text(
            text = textConverter(it),
            fontWeight = if (it == value) FontWeight.Bold else FontWeight.Normal
        )
    },
    searchable: Boolean = false,
    search: String = "",
    onSearchChange: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    anchor: DropdownPanelAnchor = DropdownPanelAnchor.BottomStart
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        DropdownMenu(
            onExpandRequest = { expanded = true }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppText(
                    text = label,
                    fontWeight = FontWeight.Medium
                )

                AppButton(
                    onClick = { expanded = !expanded },
                    backgroundColor = backgroundColor,
                    modifier = modifier,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    AppText(textConverter(value))
                    AppIcon(
                        imageVector = Lucide.ChevronDown,
                        contentDescription = "Open dropdown"
                    )
                }
            }
        }

        DropdownMenuPanel(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = modifier.appCard(backgroundColor = Theme[colors][surface2]).heightIn(max = 400.dp),
            anchor = anchor
        ) {
            if (searchable) {
                AppTextField(
                    initialValue = search,
                    placeholder = { AppText(stringResource(Res.string.search)) },
                    onValueChange = onSearchChange,
                    backgroundColor = Color.Unspecified,
                    shape = RectangleShape
                )

                HorizontalSeparator(
                    color = Theme[colors][onBackground]
                )
            }

            Row {
                val listState = rememberLazyListState()

                LaunchedEffect(value) {
                    val index = items.indexOfFirst { it == value }
                    if (index >= 0) listState.scrollToItem(index)
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxHeight().weight(1f)
                ) {
                    items.forEach {
                        item(it) {
                            Row(
                                modifier = Modifier
                                    .clickable { onValueChange(it); expanded = false }
                                    .padding(8.dp)
                                    .fillMaxWidth()
                            ) {
                                itemRenderer(it)
                            }
                        }
                    }
                }

                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(listState)
                )
            }
        }
    }
}
