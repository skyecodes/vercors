package app.vercors.core.system

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.cinterop.*
import platform.posix.uname
import platform.posix.utsname
import kotlin.coroutines.CoroutineContext

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

    actual override fun copyToClipboard(text: String): Boolean = false

    actual override suspend fun openUrlInBrowser(url: String, context: CoroutineContext): Boolean = false
}
