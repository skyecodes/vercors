package app.vercors.shared.system

import arrow.core.raise.catch
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.withContext
import oshi.PlatformEnum
import oshi.SystemInfo
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.URI
import kotlin.coroutines.CoroutineContext

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
actual class SystemServiceImpl : SystemService {
    private val systemInfo by lazy { SystemInfo() }

    actual override val osType: OsType by lazy {
        when (val platform = SystemInfo.getCurrentPlatform()) {
            PlatformEnum.WINDOWS -> OsType.Windows
            PlatformEnum.MACOS -> OsType.MacOs
            PlatformEnum.LINUX -> OsType.Linux
            else -> error("Unsupported OS platform $platform")
        }
    }

    actual override val osVersion: String by lazy {
        systemInfo.operatingSystem.versionInfo.version
    }

    actual override val osArch: OsArch by lazy {
        when (val arch = systemInfo.operatingSystem.bitness) {
            32 -> OsArch.X86
            64 -> OsArch.X64
            else -> error("Unsupported OS architecture $arch")
        }
    }

    actual override val canCopyToClipboard: Boolean by lazy {
        runCatching { Toolkit.getDefaultToolkit().systemClipboard != null }.getOrElse { false }
    }

    actual override val canOpenUrlInBrowser: Boolean by lazy {
        Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)
    }

    actual override fun copyToClipboard(text: String) = canCopyToClipboard && catch({
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
        true
    }) { false }

    actual override suspend fun openUrlInBrowser(url: String, context: CoroutineContext): Boolean =
        canOpenUrlInBrowser && catch({
            withContext(context) {
                Desktop.getDesktop().browse(URI(url))
            }
            true
        }) { false }
}
