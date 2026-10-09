package app.vercors.core.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.CoreRes
import app.vercors.core.ui.modifier.appCard
import app.vercors.core.ui.modifier.appMenuShadow
import app.vercors.core.ui.search
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.onBackground
import app.vercors.core.ui.theme.surface2
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronUp
import com.composables.icons.lucide.Lucide
import com.composeunstyled.DropdownMenuPanel
import com.composeunstyled.DropdownMenuPanelScope
import com.composeunstyled.UnstyledDropdownMenu
import com.composeunstyled.UnstyledHorizontalSeparator
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SelectFieldScaffold(
    label: String?,
    backgroundColor: Color,
    searchable: Boolean,
    search: String,
    onSearchChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    trigger: @Composable RowScope.() -> Unit,
    panel: @Composable DropdownMenuPanelScope.(closeMenu: () -> Unit) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var anchorWidth by remember { mutableStateOf(0) }
    val density = LocalDensity.current

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
                    modifier = Modifier.appMenuShadow()
                        .appCard(backgroundColor = Theme[colors][surface2])
                        .width(with(density) { anchorWidth.toDp() })
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

                    panel { expanded = false }
                }
            }
        ) {
            AppButton(
                onClick = { expanded = !expanded },
                backgroundColor = backgroundColor,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().onSizeChanged { anchorWidth = it.width }
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    trigger()
                }
                AppIcon(
                    imageVector = if (expanded) Lucide.ChevronUp else Lucide.ChevronDown,
                    contentDescription = null
                )
            }
        }
    }
}
