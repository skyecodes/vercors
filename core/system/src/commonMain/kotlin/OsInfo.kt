package app.vercors.core.system

/**
 * Immutable snapshot of the host OS identity.
 *
 * Lets logic that only needs to know *which* platform it is running on take a value instead of
 * depending on [SystemService].
 */
data class OsInfo(
    val type: OsType,
    val version: String,
    val arch: OsArch
)

val SystemService.osInfo: OsInfo
    get() = OsInfo(type = osType, version = osVersion, arch = osArch)
