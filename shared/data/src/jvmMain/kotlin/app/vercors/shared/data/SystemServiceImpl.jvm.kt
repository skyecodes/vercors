package app.vercors.shared.data

import app.vercors.shared.domain.service.CopyToClipboardResult
import app.vercors.shared.domain.service.OpenBrowserResult
import app.vercors.shared.domain.service.OsArch
import app.vercors.shared.domain.service.OsType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import oshi.PlatformEnum
import oshi.SystemInfo
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.URI

private val systemInfo = SystemInfo()

internal actual fun platformOsType(): OsType = when (val platform = SystemInfo.getCurrentPlatform()) {
    PlatformEnum.WINDOWS -> OsType.Windows
    PlatformEnum.MACOS -> OsType.MacOs
    PlatformEnum.LINUX -> OsType.Linux
    else -> error("Unsupported OS platform $platform")
}

internal actual fun platformOsVersion(): String = systemInfo.operatingSystem.versionInfo.version

internal actual fun platformOsArch(): OsArch = when (val arch = systemInfo.operatingSystem.bitness) {
    32 -> OsArch.X86
    64 -> OsArch.X64
    else -> error("Unsupported OS architecture $arch")
}

internal actual fun platformCopyToClipboard(text: String): CopyToClipboardResult = try {
    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
    CopyToClipboardResult.Success
} catch (e: Exception) {
    CopyToClipboardResult.Failure(e)
}

internal actual suspend fun platformOpenUrlInBrowser(
    url: String, ioDispatcher: CoroutineDispatcher
): OpenBrowserResult {
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
