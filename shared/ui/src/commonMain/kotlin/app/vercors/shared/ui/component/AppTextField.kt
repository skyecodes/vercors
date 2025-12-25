package app.vercors.shared.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import app.vercors.shared.ui.theme.colors
import app.vercors.shared.ui.theme.surface
import com.composeunstyled.LocalContentColor
import com.composeunstyled.ProvideContentColor
import com.composeunstyled.TextField
import com.composeunstyled.TextInput
import com.composeunstyled.theme.Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AppTextField(
    label: String? = null,
    initialValue: String,
    placeholder: (@Composable () -> Unit)? = null,
    onValueChange: (String) -> Unit = {},
    debounceMillis: Long = 0L,
    imeAction: ImeAction = ImeAction.Unspecified,
    editable: Boolean = true,
    shape: Shape = RoundedCornerShape(4.dp),
    backgroundColor: Color = Theme[colors][surface],
    modifier: Modifier = Modifier
) {
    val state = rememberTextFieldState(initialValue)

    LaunchedEffect(state) {
        snapshotFlow { state.text.toString() }.collectLatest {
            delay(debounceMillis)
            onValueChange(it)
        }
    }

    TextField(
        state = state,
        modifier = modifier,
        singleLine = true,
        cursorBrush = SolidColor(LocalContentColor.current),
        editable = editable,
        onKeyboardAction = {
            onValueChange(state.text.toString())
            it()
        },
        keyboardOptions = KeyboardOptions(
            imeAction = imeAction
        )
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            label?.let {
                AppText(
                    text = label,
                    fontWeight = FontWeight.Medium
                )
            }

            TextInput(
                shape = shape,
                backgroundColor = backgroundColor,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 7.5.dp),
                placeholder = placeholder?.let {
                    { ProvideContentColor(LocalContentColor.current.copy(alpha = 0.66f), it) }
                }
            )
        }
    }
}
