package app.vercors.feature.project.ui.install

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.component.*
import app.vercors.core.ui.layout.AppDialog
import app.vercors.core.ui.layout.AppForm
import app.vercors.core.ui.theme.*
import app.vercors.feature.project.ui.*
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.cancel
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.stringResource

@Composable
fun InstallModpack(
    store: Store<InstallModpackState, InstallModpackEvent>,
) {
    val (state, eventSink) = store

    AppDialog(onClickOutside = { eventSink(InstallModpackEvent.OnCancelClick) }) {
        AppForm {
            AppText(
                text = stringResource(ProjectRes.string.installModpack),
                style = Theme[textStyles][title]
            )

            AppSelectField(
                label = stringResource(ProjectRes.string.`file`),
                value = state.selectedFile,
                items = state.fileList,
                onValueChange = { eventSink(InstallModpackEvent.OnFileSelected(it?.id)) },
                itemRenderer = { value, _ -> AppSelectItem(text = value?.displayName ?: "") },
                backgroundColor = Theme[colors][surface2],
                modifier = Modifier.width(300.dp),
            )

            AppTextField(
                label = stringResource(ProjectRes.string.instanceName),
                initialValue = state.instanceName,
                onValueChange = { eventSink(InstallModpackEvent.OnInstanceNameChange(it)) },
                backgroundColor = Theme[colors][surface2],
                modifier = Modifier.width(300.dp),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppIconTextButton(
                    onClick = { eventSink(InstallModpackEvent.OnCancelClick) },
                    text = stringResource(SharedRes.string.cancel),
                    imageVector = Lucide.X,
                )
                AppIconTextButton(
                    onClick = { eventSink(InstallModpackEvent.OnConfirmClick) },
                    text = stringResource(ProjectRes.string.install),
                    imageVector = Lucide.Download,
                    backgroundColor = Theme[colors][primary],
                    contentColor = Theme[colors][onPrimary],
                    enabled = state.canInstall
                )
            }
        }
    }
}
