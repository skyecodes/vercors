package app.vercors.feature.instance.ui.copy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.component.AppIconTextButton
import app.vercors.core.ui.component.AppText
import app.vercors.core.ui.component.AppTextField
import app.vercors.core.ui.layout.AppDialog
import app.vercors.core.ui.layout.AppForm
import app.vercors.core.ui.theme.*
import app.vercors.feature.instance.ui.InstanceRes
import app.vercors.feature.instance.ui.copyInstance
import app.vercors.feature.instance.ui.instanceName
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.cancel
import app.vercors.shared.ui.copy
import com.composables.icons.lucide.Copy
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.stringResource

@Composable
fun CopyInstance(
    store: Store<CopyInstanceState, CopyInstanceEvent>,
    modifier: Modifier = Modifier,
) {
    val (state, eventSink) = store

    AppDialog(
        modifier = modifier,
        onClickOutside = { eventSink(CopyInstanceEvent.OnCancelClick) }
    ) {
        AppForm {
            AppText(
                text = stringResource(InstanceRes.string.copyInstance),
                style = Theme[textStyles][title]
            )

            AppTextField(
                label = stringResource(InstanceRes.string.instanceName),
                initialValue = state.instanceName,
                onValueChange = { eventSink(CopyInstanceEvent.OnInstanceNameChange(it)) },
                enabled = !state.isCopying,
                modifier = Modifier.width(300.dp),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppIconTextButton(
                    onClick = { eventSink(CopyInstanceEvent.OnCancelClick) },
                    text = stringResource(SharedRes.string.cancel),
                    imageVector = Lucide.X,
                    enabled = !state.isCopying,
                )
                AppIconTextButton(
                    onClick = { eventSink(CopyInstanceEvent.OnCopyClick) },
                    text = stringResource(SharedRes.string.copy),
                    imageVector = Lucide.Copy,
                    backgroundColor = Theme[colors][primary],
                    contentColor = Theme[colors][onPrimary],
                    enabled = state.canCopyInstance
                )
            }
        }
    }
}
