package app.vercors.shared.data

import app.vercors.shared.domain.service.*
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.annotation.Single

@Single
class SystemServiceImpl(
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : SystemService {
    override val osType: OsType by lazy { platformOsType() }
    override val osVersion: String by lazy { platformOsVersion() }
    override val osArch: OsArch by lazy { platformOsArch() }

    override fun copyToClipboard(text: String): CopyToClipboardResult = platformCopyToClipboard(text)

    override suspend fun openUrlInBrowser(url: String): OpenBrowserResult = platformOpenUrlInBrowser(url, ioDispatcher)
}

internal expect fun platformOsType(): OsType

internal expect fun platformOsVersion(): String

internal expect fun platformOsArch(): OsArch

internal expect fun platformCopyToClipboard(text: String): CopyToClipboardResult

internal expect suspend fun platformOpenUrlInBrowser(url: String, ioDispatcher: CoroutineDispatcher): OpenBrowserResult
