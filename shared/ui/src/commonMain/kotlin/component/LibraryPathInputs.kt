package app.vercors.shared.ui.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.vercors.core.ui.component.AppCheckbox
import app.vercors.core.ui.component.AppIconTextButton
import app.vercors.core.ui.component.AppText
import app.vercors.core.ui.component.AppTextField
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.surface2
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.browse
import app.vercors.shared.ui.useDefaultPath
import com.composables.icons.lucide.FolderOpen
import com.composables.icons.lucide.Lucide
import com.composeunstyled.theme.Theme
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path
import org.jetbrains.compose.resources.stringResource

@Composable
fun RowScope.LibraryPathInputs(
    checked: Boolean,
    path: String,
    detectedPath: String?,
    onCheckedChange: (Boolean) -> Unit,
    onPathChange: (String) -> Unit,
) {
    val pickerLauncher = rememberFilePickerLauncher(type = FileKitType.File("so")) { file ->
        file?.let { onPathChange(it.path) }
    }
    AppCheckbox(checked = checked, onCheckedChange = onCheckedChange)
    AppTextField(
        initialValue = path,
        onValueChange = onPathChange,
        debounceMillis = 500,
        enabled = checked,
        placeholder = { AppText(text = detectedPath ?: stringResource(SharedRes.string.useDefaultPath)) },
        backgroundColor = Theme[colors][surface2],
        modifier = Modifier.weight(1f)
    )
    AppIconTextButton(
        onClick = pickerLauncher::launch,
        text = stringResource(SharedRes.string.browse),
        imageVector = Lucide.FolderOpen,
        enabled = checked,
    )
}
