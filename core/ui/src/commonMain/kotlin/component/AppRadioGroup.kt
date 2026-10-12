package app.vercors.core.ui.component

import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.modifier.disabledAlpha
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.onBackground
import app.vercors.core.ui.theme.primary
import com.composeunstyled.*
import com.composeunstyled.theme.Theme
import kotlin.jvm.JvmInline

@Composable
fun <T> AppRadioGroup(
    value: T?,
    onValueChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    accessibilityLabel: String? = null,
    content: @Composable AppRadioGroupScope.() -> Unit,
) {
    UnstyledRadioGroup(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        accessibilityLabel = accessibilityLabel
    ) {
        val scope = remember(this) { AppRadioGroupScope(this) }
        scope.content()
    }
}

@JvmInline
value class AppRadioGroupScope(val scope: RadioGroupScope)

@Composable
fun <T> AppRadioGroupScope.AppRadioButton(
    value: T,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = null,
    content: @Composable AppRadioButtonScope.() -> Unit,
) {
    scope.RadioButton(
        value = value,
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        indication = indication
    ) {
        val scope = remember(this) { AppRadioButtonScope(this) }
        scope.content()
    }
}

@JvmInline
value class AppRadioButtonScope(val scope: RadioButtonScope)

@Composable
fun <T> AppRadioGroupScope.AppRadioButton(
    value: T,
    label: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    invert: Boolean = false,
    borderColor: Color = Theme[colors][onBackground],
    selectedColor: Color = Theme[colors][primary],
    radioSize: Dp = 24.dp,
) {
    AppRadioButton(
        value = value,
        modifier = modifier
            .then(if (enabled) Modifier.pointerHoverIcon(PointerIcon.Hand) else Modifier)
            .clip(RoundedCornerShape(4.dp)),
        enabled = enabled,
        indication = LocalIndication.current,
    ) {
        Row(
            modifier = Modifier.padding(ThinButtonContentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (label != null && !invert) AppText(text = label, modifier = Modifier.disabledAlpha(enabled))
            RadioIndicator(
                borderColor = borderColor,
                selectedColor = selectedColor,
                radioSize = radioSize,
                modifier = Modifier.disabledAlpha(enabled),
            )
            if (label != null && invert) AppText(text = label, modifier = Modifier.disabledAlpha(enabled))
        }
    }
}

@Composable
private fun AppRadioButtonScope.RadioIndicator(
    borderColor: Color,
    selectedColor: Color,
    radioSize: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(radioSize)
            .border(width = 2.dp, color = borderColor, shape = CircleShape)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        with(scope) {
            SelectedIndicator(
                modifier = Modifier.fillMaxSize(),
                enter = fadeIn(spring()),
                exit = fadeOut(spring()),
            ) {
                Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(selectedColor))
            }
        }
    }
}
