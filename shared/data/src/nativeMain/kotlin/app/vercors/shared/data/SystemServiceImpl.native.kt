package app.vercors.shared.data

import app.vercors.shared.domain.service.CopyToClipboardResult
import app.vercors.shared.domain.service.OpenBrowserResult
import kotlinx.coroutines.CoroutineDispatcher

internal actual suspend fun platformOpenUrlInBrowser(
    url: String,
    ioDispatcher: CoroutineDispatcher
): OpenBrowserResult =
    OpenBrowserResult.Unsupported

internal actual fun platformCopyToClipboard(text: String): CopyToClipboardResult = CopyToClipboardResult.Unsupported
