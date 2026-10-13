package app.vercors.feature.instance.ui.export

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.path

@Composable
actual fun rememberExportFileSaver(onResult: (path: String?) -> Unit): (suggestedName: String, extension: String) -> Unit {
    val launcher = rememberFileSaverLauncher(FileKitDialogSettings.createDefault()) { file -> onResult(file?.path) }
    return remember(launcher) {
        { suggestedName, extension ->
            launcher.launch(
                suggestedName = suggestedName,
                defaultExtension = extension,
                allowedExtensions = setOf(extension)
            )
        }
    }
}
