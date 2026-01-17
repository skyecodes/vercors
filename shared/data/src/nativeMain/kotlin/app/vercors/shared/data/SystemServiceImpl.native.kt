package app.vercors.shared.data

import app.vercors.shared.domain.service.CopyToClipboardResult
import app.vercors.shared.domain.service.OpenBrowserResult
import kotlin.coroutines.CoroutineContext

internal actual suspend fun platformOpenUrlInBrowser(
    url: String,
    context: CoroutineContext
): OpenBrowserResult = OpenBrowserResult.Unsupported

internal actual fun platformCopyToClipboard(text: String): CopyToClipboardResult = CopyToClipboardResult.Unsupported
