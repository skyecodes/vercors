package app.vercors.core.system

data class OsInfo(
    val type: OsType,
    val version: String,
    val arch: OsArch
)

val SystemService.osInfo: OsInfo
    get() = OsInfo(type = osType, version = osVersion, arch = osArch)
