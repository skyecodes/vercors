package app.vercors.core.system

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.cinterop.*
import platform.posix.*

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
actual class SystemServiceImpl : SystemService {
    actual override val osType: OsType = OsType.MacOs

    @OptIn(ExperimentalForeignApi::class)
    actual override val osVersion: String by lazy {
        memScoped {
            val res = alloc<utsname>()
            uname(res.ptr)
            res.version.toKString()
        }
    }

    actual override val osArch: OsArch = OsArch.X64

    actual override val canCopyToClipboard: Boolean = false

    actual override val canOpenUrlInBrowser: Boolean = false

    actual override val canOpenFileManager: Boolean = false

    @OptIn(ExperimentalForeignApi::class)
    actual override val totalPhysicalMemoryBytes: Long? by lazy {
        try {
            val process = popen("sysctl -n hw.memsize", "r") ?: return@lazy null
            val buffer = ByteArray(64)
            val line = fgets(buffer.refTo(0), buffer.size, process)?.toKString()
            pclose(process)
            line?.trim()?.toLongOrNull()
        } catch (_: Exception) {
            null
        }
    }

    actual override fun copyToClipboard(text: String): Boolean = false

    @OptIn(ExperimentalForeignApi::class)
    actual override fun downloadsDir(): String? = getenv("HOME")?.toKString()?.let { "$it/Downloads" }

    actual override fun openInFileManager(path: String): Boolean = false

}
