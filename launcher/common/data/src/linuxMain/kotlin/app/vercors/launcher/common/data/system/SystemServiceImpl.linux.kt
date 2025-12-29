package app.vercors.launcher.common.data.system

import app.vercors.launcher.common.domain.system.OsArch
import app.vercors.launcher.common.domain.system.OsType
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

internal actual fun getOsName(): OsType = OsType.Linux

internal actual fun getOsVersion(): String = versionAndArch.first

internal actual fun getOsArch(): OsArch = if (versionAndArch.second.endsWith("64")) OsArch.X64 else OsArch.X86
