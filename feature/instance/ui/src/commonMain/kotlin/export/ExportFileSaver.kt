package app.vercors.feature.instance.ui.export

import androidx.compose.runtime.Composable

// FileKit's save dialog only exists on non-web targets, and this module also compiles to JS for the website demo
@Composable
expect fun rememberExportFileSaver(onResult: (path: String?) -> Unit): (suggestedName: String, extension: String) -> Unit
