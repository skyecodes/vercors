package app.vercors.shared.ui.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.vercors.core.ui.component.AppCheckbox
import app.vercors.core.ui.component.AppNumberField
import app.vercors.core.ui.component.AppSlider
import app.vercors.core.ui.component.AppText
import app.vercors.core.ui.theme.label
import app.vercors.core.ui.theme.textStyles
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.megabytes
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.stringResource

@Composable
fun RowScope.MemorySliderInputs(
    checked: Boolean,
    value: Int,
    valueRange: IntRange,
    stepSize: Int,
    onCheckedChange: (Boolean) -> Unit,
    onValueChange: (Int) -> Unit,
) {
    val displayValue = value.coerceIn(valueRange)
    AppCheckbox(checked = checked, onCheckedChange = onCheckedChange)
    AppSlider(
        value = displayValue,
        onValueChange = onValueChange,
        valueRange = valueRange,
        stepSize = stepSize,
        enabled = checked,
        modifier = Modifier.weight(1f)
    )
    AppNumberField(
        value = displayValue,
        onValueChange = onValueChange,
        valueRange = valueRange,
        enabled = checked,
    )
    AppText(text = stringResource(SharedRes.string.megabytes), style = Theme[textStyles][label])
}
