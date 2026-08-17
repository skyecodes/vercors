package app.vercors.core.system

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.cinterop.*
import platform.posix.uname
import platform.posix.utsname

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

    actual override fun copyToClipboard(text: String): Boolean = false

}
