package app.vercors.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
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
import app.vercors.core.ui.theme.surface
import com.composeunstyled.*
import com.composeunstyled.theme.Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
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
    backgroundColor: Color = Theme[colors][surface],
    modifier: Modifier = Modifier
) {
    val state = rememberTextFieldState(initialValue)

    LaunchedEffect(state) {
        snapshotFlow { state.text.toString() }.collectLatest {
            delay(debounceMillis.milliseconds)
            onValueChange(it)
        }
    }

    UnstyledTextField(
        state = state,
        modifier = modifier.width(IntrinsicSize.Max),
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
