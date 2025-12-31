package app.vercors.shared.data

import app.vercors.shared.domain.service.OsArch
import app.vercors.shared.domain.service.OsType
import kotlinx.cinterop.*
import platform.posix.NULL
import platform.posix._pclose
import platform.posix._popen
import platform.posix.fgets

private const val BUFFER_SIZE = 1024
private val VERSION_REGEX = Regex("((\\d+\\.)+\\d+)")

internal actual fun platformOsType(): OsType = OsType.Windows

@OptIn(ExperimentalForeignApi::class)
internal actual fun platformOsVersion(): String = memScoped {
    val buffer = allocArray<ByteVar>(BUFFER_SIZE)
    val pipe = _popen("ver", "r")
    var out = ""
    while (fgets(buffer, BUFFER_SIZE, pipe) != NULL) {
        out += buffer.toKString()
    }
    _pclose(pipe)
    VERSION_REGEX.find(out)?.groupValues?.getOrNull(1) ?: "unknown"
}

internal actual fun platformOsArch(): OsArch = OsArch.X64
