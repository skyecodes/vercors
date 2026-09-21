package app.vercors.feature.config.ui.datadirectory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.component.AppIconTextButton
import app.vercors.core.ui.component.AppProgressIndicator
import app.vercors.core.ui.component.AppText
import app.vercors.core.ui.component.AppTextField
import app.vercors.core.ui.layout.AppDialog
import app.vercors.core.ui.layout.AppForm
import app.vercors.core.ui.theme.*
import app.vercors.feature.config.ui.*
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.cancel
import com.composables.icons.lucide.*
import com.composeunstyled.theme.Theme
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.path
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChangeDataDirectory(
    store: Store<ChangeDataDirectoryState, ChangeDataDirectoryEvent>,
) {
    val (state, eventSink) = store

    val directoryPicker = rememberDirectoryPickerLauncher { directory ->
        directory?.path?.let { eventSink(ChangeDataDirectoryEvent.OnPathChange(it)) }
    }

    AppDialog(
        modifier = Modifier.width(480.dp),
        onClickOutside = { if (!state.isMoving) eventSink(ChangeDataDirectoryEvent.OnCancelClick) },
    ) {
        AppForm {
            AppText(text = stringResource(ConfigRes.string.changeDataDirectoryTitle), style = Theme[textStyles][title])
            AppText(
                text = stringResource(ConfigRes.string.changeDataDirectoryDescription),
                style = Theme[textStyles][label]
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                AppTextField(
                    initialValue = state.pathText,
                    onValueChange = { eventSink(ChangeDataDirectoryEvent.OnPathChange(it)) },
                    enabled = !state.isMoving,
                    modifier = Modifier.weight(1f, fill = false).width(260.dp),
                )
                AppIconTextButton(
                    onClick = directoryPicker::launch,
                    text = stringResource(ConfigRes.string.changeDataDirectorySelectFolder),
                    imageVector = Lucide.FolderOpen,
                    enabled = !state.isMoving,
                )
                AppIconTextButton(
                    onClick = { eventSink(ChangeDataDirectoryEvent.OnResetClick) },
                    text = stringResource(ConfigRes.string.changeDataDirectoryReset),
                    imageVector = Lucide.RotateCcw,
                    enabled = !state.isMoving,
                )
            }

            if (state.isMoving) {
                AppProgressIndicator(progress = state.moveProgress ?: 0f, modifier = Modifier.width(300.dp))
            }

            state.errorMessage?.let {
                AppText(text = it, color = Theme[colors][error])
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppIconTextButton(
                    onClick = { eventSink(ChangeDataDirectoryEvent.OnCancelClick) },
                    text = stringResource(SharedRes.string.cancel),
                    imageVector = Lucide.X,
                    enabled = !state.isMoving,
                )
                AppIconTextButton(
                    onClick = { eventSink(ChangeDataDirectoryEvent.OnConfirmClick) },
                    text = stringResource(ConfigRes.string.changeDataDirectoryConfirm),
                    imageVector = Lucide.Check,
                    backgroundColor = Theme[colors][primary],
                    contentColor = Theme[colors][onPrimary],
                    enabled = state.canConfirm,
                )
            }
        }
    }
}
