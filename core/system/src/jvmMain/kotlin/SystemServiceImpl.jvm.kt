package app.vercors.core.system

import arrow.core.raise.catch
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import oshi.SystemInfo
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File

private val logger = KotlinLogging.logger {}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
actual class SystemServiceImpl(
    coroutineScope: CoroutineScope
) : SystemService {
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

    override val canCopyToClipboard: Boolean by lazy {
        runCatching { Toolkit.getDefaultToolkit().systemClipboard != null }.getOrElse { false }
    }

    override val canOpenUrlInBrowser: Boolean by lazy {
        Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)
    }

    override val canOpenFileManager: Boolean by lazy {
        Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)
    }

    // SystemInfo() performs slow native hardware enumeration (JNA) — the first synchronous access to this
    // property used to cause a noticeable UI freeze (e.g. opening Settings or an instance's details for the
    // first time). Warm it on a background thread as soon as this singleton is constructed instead; the
    // getter below only blocks if something reads the property before that warm-up finishes.
    @Volatile
    private var cachedTotalPhysicalMemoryBytes: Long? = null

    init {
        coroutineScope.launch(Dispatchers.IO) {
            cachedTotalPhysicalMemoryBytes = queryTotalPhysicalMemoryBytes()
        }
    }

    private fun queryTotalPhysicalMemoryBytes(): Long? =
        runCatching { SystemInfo().hardware.memory.total }.getOrNull()

    actual override val totalPhysicalMemoryBytes: Long?
        get() = cachedTotalPhysicalMemoryBytes
            ?: queryTotalPhysicalMemoryBytes().also { cachedTotalPhysicalMemoryBytes = it }

    override fun copyToClipboard(text: String) = canCopyToClipboard && catch({
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
        true
    }) {
        logger.warn(it) { "Unable to copy content to clipboard" }
        false
    }

    actual override fun downloadsDir(): String? =
        System.getProperty("user.home")?.let { "$it/Downloads" }

    override fun openInFileManager(path: String): Boolean = canOpenFileManager && catch({
        Desktop.getDesktop().open(File(path))
        true
    }) {
        logger.warn(it) { "Unable to open $path in the file manager" }
        false
    }

}
