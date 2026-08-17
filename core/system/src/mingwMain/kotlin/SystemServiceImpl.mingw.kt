package app.vercors.core.system

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.cinterop.*
import platform.posix.NULL
import platform.posix._pclose
import platform.posix._popen
import platform.posix.fgets

private const val BUFFER_SIZE = 1024
private val VERSION_REGEX = Regex("((\\d+\\.)+\\d+)")

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
actual class SystemServiceImpl : SystemService {
    actual override val osType: OsType = OsType.Windows

    @OptIn(ExperimentalForeignApi::class)
    actual override val osVersion: String by lazy {
        memScoped {
            val buffer = allocArray<ByteVar>(BUFFER_SIZE)
            val pipe = _popen("ver", "r")
            var out = ""
            while (fgets(buffer, BUFFER_SIZE, pipe) != NULL) {
                out += buffer.toKString()
            }
            _pclose(pipe)
            VERSION_REGEX.find(out)?.groupValues?.getOrNull(1) ?: "unknown"
        }
    }

    actual override val osArch: OsArch = OsArch.X64

    actual override val canCopyToClipboard: Boolean = false

    actual override val canOpenUrlInBrowser: Boolean = false

    actual override fun copyToClipboard(text: String): Boolean = false

}
