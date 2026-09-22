package app.vercors.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import app.vercors.core.ui.CoreRes
import app.vercors.core.ui.search
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.surface2
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppSearchField(
    initialValue: String,
    onValueChange: (String) -> Unit,
    placeholder: String = stringResource(CoreRes.string.search),
    modifier: Modifier = Modifier,
    debounceMillis: Long = 0L,
    backgroundColor: Color = Theme[colors][surface2],
) {
    AppTextField(
        initialValue = initialValue,
        onValueChange = onValueChange,
        placeholder = { AppText(placeholder) },
        debounceMillis = debounceMillis,
        imeAction = ImeAction.Search,
        backgroundColor = backgroundColor,
        modifier = modifier,
    )
}
