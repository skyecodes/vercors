package app.vercors.core.system

import arrow.core.raise.catch
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.URI
import kotlin.coroutines.CoroutineContext

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
actual class SystemServiceImpl : SystemService {
    actual override val osType: OsType = System.getProperty("os.name").lowercase().let { name ->
        when {
            name.startsWith("windows") -> OsType.Windows
            name.startsWith("mac") || name.startsWith("darwin") -> OsType.MacOs
            name.contains("linux") || name.contains("nix") || name.contains("nux") -> OsType.Linux
            else -> error("Unsupported OS platform $name")
        }
    }

    actual override val osVersion: String = System.getProperty("os.version")

    actual override val osArch: OsArch =
        if (System.getProperty("os.arch").endsWith("64")) OsArch.X64 else OsArch.X86

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
