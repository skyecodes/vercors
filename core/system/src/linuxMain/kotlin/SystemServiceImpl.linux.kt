package app.vercors.core.system

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.cinterop.*
import platform.posix.getenv
import platform.posix.uname
import platform.posix.utsname

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
actual class SystemServiceImpl : SystemService {
    @OptIn(ExperimentalForeignApi::class)
    private val versionAndArch: Pair<String, String> by lazy {
        memScoped {
            val name = alloc<utsname>()
            uname(name.ptr)
            name.release.toKString() to name.machine.toKString()
        }
    }

    actual override val osType: OsType = OsType.Linux

    actual override val osVersion: String by lazy { versionAndArch.first }

    actual override val osArch: OsArch by lazy { if (versionAndArch.second.endsWith("64")) OsArch.X64 else OsArch.X86 }

    actual override val canCopyToClipboard: Boolean = false

    actual override val canOpenUrlInBrowser: Boolean = false

    actual override val canOpenFileManager: Boolean = false

    actual override fun copyToClipboard(text: String): Boolean = false

    @OptIn(ExperimentalForeignApi::class)
    actual override fun downloadsDir(): String? = getenv("HOME")?.toKString()?.let { "$it/Downloads" }

    actual override fun openInFileManager(path: String): Boolean = false

}
