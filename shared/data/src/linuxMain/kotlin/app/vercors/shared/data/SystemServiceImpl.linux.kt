package app.vercors.shared.data

import app.vercors.shared.domain.service.OsArch
import app.vercors.shared.domain.service.OsType
import kotlinx.cinterop.*
import platform.posix.uname
import platform.posix.utsname

@OptIn(ExperimentalForeignApi::class)
private val versionAndArch: Pair<String, String> by lazy {
    val name = nativeHeap.alloc<utsname>()
    uname(name.ptr)
    val result = name.release.toKString() to name.machine.toKString()
    nativeHeap.free(name)
    result
}

internal actual fun platformOsType(): OsType = OsType.Linux

internal actual fun platformOsVersion(): String = versionAndArch.first

internal actual fun platformOsArch(): OsArch = if (versionAndArch.second.endsWith("64")) OsArch.X64 else OsArch.X86
