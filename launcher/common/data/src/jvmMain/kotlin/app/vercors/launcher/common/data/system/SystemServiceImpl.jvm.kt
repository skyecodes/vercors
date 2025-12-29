package app.vercors.launcher.common.data.system

import app.vercors.launcher.common.domain.system.OsArch
import app.vercors.launcher.common.domain.system.OsType
import oshi.PlatformEnum
import oshi.SystemInfo

private val systemInfo = SystemInfo()

internal actual fun getOsName(): OsType = when (val platform = SystemInfo.getCurrentPlatform()) {
    PlatformEnum.WINDOWS -> OsType.Windows
    PlatformEnum.MACOS -> OsType.MacOs
    PlatformEnum.LINUX -> OsType.Linux
    else -> error("Unsupported OS platform $platform")
}

internal actual fun getOsVersion(): String = systemInfo.operatingSystem.versionInfo.version

internal actual fun getOsArch(): OsArch = when (val arch = systemInfo.operatingSystem.bitness) {
    32 -> OsArch.X86
    64 -> OsArch.X64
    else -> error("Unsupported OS architecture $arch")
}
