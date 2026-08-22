package app.vercors.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.primary
import app.vercors.core.ui.theme.surface2
import com.composeunstyled.*
import com.composeunstyled.theme.Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlin.time.Duration.Companion.milliseconds

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
    backgroundColor: Color = Theme[colors][surface2],
    modifier: Modifier = Modifier
) {
    val state = rememberTextFieldState(initialValue)
    var lastKnownExternalValue by remember { mutableStateOf(initialValue) }

    LaunchedEffect(initialValue) {
        lastKnownExternalValue = initialValue
        if (state.text.toString() != initialValue) {
            state.edit {
                replace(0, length, initialValue)
            }
        }
    }

    LaunchedEffect(state) {
        // Drop the current value emitted on subscribe so remounting this composable (e.g. navigating
        // back to a screen that retained its state) doesn't spuriously re-fire onValueChange. Also skip
        // any emission that matches the last value we received via `initialValue` — otherwise a caller
        // whose `initialValue` changes for reasons other than the user typing (e.g. an external value
        // being reset by some other control) gets its own correction echoed straight back through
        // `onValueChange`, since the sync above mutates `state` the same way a keystroke would.
        snapshotFlow { state.text.toString() }.drop(1).collectLatest {
            delay(debounceMillis.milliseconds)
            if (it != lastKnownExternalValue) {
                onValueChange(it)
            }
        }
    }

    Column(
        modifier = modifier.width(IntrinsicSize.Max),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        label?.let {
            AppText(
                text = label,
                fontWeight = FontWeight.Medium
            )
        }

        UnstyledTextField(
            state = state,
            modifier = Modifier.fillMaxWidth(),
            cursorBrush = SolidColor(LocalContentColor.current),
            readOnly = !editable,
            onKeyboardAction = {
                onValueChange(state.text.toString())
                it()
            },
            keyboardOptions = KeyboardOptions(
                imeAction = imeAction
            ),
            textStyle = LocalTextStyle.current,
            textColor = LocalContentColor.current,
            selectionColors = TextSelectionColors(
                handleColor = Theme[colors][primary],
                backgroundColor = Theme[colors][primary].copy(alpha = 0.2f),
            )
        ) {
            TextInput(
                modifier = Modifier
                    .clip(shape)
                    .background(backgroundColor)
                    .padding(horizontal = 8.dp, vertical = 7.5.dp)
                    .fillMaxWidth(),
                placeholder = placeholder?.let {
                    { ProvideContentColor(LocalContentColor.current.copy(alpha = 0.66f), it) }
                }
            )
        }
    }
}
