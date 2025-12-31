package app.vercors.shared.data

import app.vercors.shared.domain.service.OsArch
import app.vercors.shared.domain.service.OsType
import kotlinx.cinterop.*
import platform.posix.uname
import platform.posix.utsname

internal actual fun platformOsType(): OsType = OsType.MacOs

@OptIn(ExperimentalForeignApi::class)
internal actual fun platformOsVersion(): String {
    val res = nativeHeap.alloc<utsname>()
    uname(res.ptr)
    val version = res.version.toKString()
    nativeHeap.free(res)
    return version
}

internal actual fun platformOsArch(): OsArch = OsArch.X64
