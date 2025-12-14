package app.vercors.shared.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import app.vercors.shared.ui.colors
import app.vercors.shared.ui.onBackground
import app.vercors.shared.ui.surface
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.composeunstyled.Checkbox
import com.composeunstyled.theme.Theme

@Composable
fun AppCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AppText(label)
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            shape = RoundedCornerShape(4.dp),
            backgroundColor = Theme[colors][surface],
            contentColor = Theme[colors][onBackground],
            modifier = Modifier.size(32.dp).pointerHoverIcon(PointerIcon.Hand)
        ) {
            AppIcon(
                imageVector = Lucide.Check,
                contentDescription = null,
            )
        }
    }
}
