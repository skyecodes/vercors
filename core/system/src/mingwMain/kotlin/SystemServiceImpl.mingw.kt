package app.vercors.core.system

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.cinterop.*
import platform.posix.*
import platform.windows.GlobalMemoryStatusEx
import platform.windows.MEMORYSTATUSEX

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

    @OptIn(ExperimentalForeignApi::class)
    actual override val totalPhysicalMemoryBytes: Long? by lazy {
        memScoped {
            val status = alloc<MEMORYSTATUSEX>()
            status.dwLength = sizeOf<MEMORYSTATUSEX>().toUInt()
            if (GlobalMemoryStatusEx(status.ptr) != 0) status.ullTotalPhys.toLong() else null
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual override fun downloadsDir(): String? = getenv("USERPROFILE")?.toKString()?.let { "$it\\Downloads" }

}
