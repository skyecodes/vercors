package app.vercors.shared.data

import app.vercors.shared.domain.service.*
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlin.coroutines.CoroutineContext

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class SystemServiceImpl : SystemService {
    override val osType: OsType by lazy { platformOsType() }
    override val osVersion: String by lazy { platformOsVersion() }
    override val osArch: OsArch by lazy { platformOsArch() }

    override fun copyToClipboard(text: String): CopyToClipboardResult = platformCopyToClipboard(text)

    override suspend fun openUrlInBrowser(url: String): OpenBrowserResult = platformOpenUrlInBrowser(url)
}

internal expect fun platformOsType(): OsType

internal expect fun platformOsVersion(): String

internal expect fun platformOsArch(): OsArch

internal expect fun platformCopyToClipboard(text: String): CopyToClipboardResult

internal expect suspend fun platformOpenUrlInBrowser(
    url: String,
    context: CoroutineContext = Dispatchers.IO
): OpenBrowserResult
