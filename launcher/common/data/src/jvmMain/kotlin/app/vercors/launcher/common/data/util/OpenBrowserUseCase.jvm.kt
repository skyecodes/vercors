package app.vercors.launcher.common.data.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.net.URI

actual suspend fun openUrlInBrowser(url: String, ioDispatcher: CoroutineDispatcher): OpenBrowserResult {
    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
        try {
            withContext(ioDispatcher) {
                Desktop.getDesktop().browse(URI(url))
                OpenBrowserResult.Success
            }
        } catch (e: Exception) {
            return OpenBrowserResult.Failure(e)
        }
    }
    return OpenBrowserResult.Unsupported
}
