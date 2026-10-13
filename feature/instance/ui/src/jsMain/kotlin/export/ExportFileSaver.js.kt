package app.vercors.feature.instance.ui.export

import androidx.compose.runtime.Composable

@Composable
actual fun rememberExportFileSaver(onResult: (path: String?) -> Unit): (suggestedName: String, extension: String) -> Unit =
    { _, _ -> onResult(null) }
