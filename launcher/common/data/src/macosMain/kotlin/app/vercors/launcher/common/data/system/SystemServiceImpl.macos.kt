package app.vercors.launcher.common.data.system

import app.vercors.launcher.common.domain.system.OsArch
import app.vercors.launcher.common.domain.system.OsType
import kotlinx.cinterop.*
import platform.posix.uname
import platform.posix.utsname

internal actual fun getOsName(): OsType = OsType.MacOs

@OptIn(ExperimentalForeignApi::class)
internal actual fun getOsVersion(): String {
    val res = nativeHeap.alloc<utsname>()
    uname(res.ptr)
    val version = res.version.toKString()
    nativeHeap.free(res)
    return version
}

internal actual fun getOsArch(): OsArch = OsArch.X64
