package app.vercors.feature.project.ui.install

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.component.*
import app.vercors.core.ui.layout.AppDialog
import app.vercors.core.ui.layout.AppForm
import app.vercors.core.ui.theme.*
import app.vercors.feature.project.ui.ProjectRes
import app.vercors.feature.project.ui.file
import app.vercors.feature.project.ui.install
import app.vercors.feature.project.ui.installModpack
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.cancel
import app.vercors.shared.ui.instanceName
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.stringResource

@Composable
fun InstallModpack(
    store: Store<InstallModpackState, InstallModpackEvent>,
    modifier: Modifier = Modifier,
) {
    val (state, eventSink) = store

    AppDialog(
        modifier = modifier,
        onClickOutside = { eventSink(InstallModpackEvent.OnCancelClick) }
    ) {
        AppForm(modifier = Modifier.width(IntrinsicSize.Max)) {
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
                modifier = Modifier.width(300.dp),
            )

            AppTextField(
                label = stringResource(SharedRes.string.instanceName),
                initialValue = state.instanceName,
                onValueChange = { eventSink(InstallModpackEvent.OnInstanceNameChange(it)) },
                modifier = Modifier.width(300.dp),
            )

            if (state.installing) {
                AppDownloadProgress(
                    progress = state.installProgress.fraction,
                    download = state.installProgress.download,
                    modifier = Modifier.fillMaxWidth()
                )
            }

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
