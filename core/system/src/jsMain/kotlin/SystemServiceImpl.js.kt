package app.vercors.core.system

actual class SystemServiceImpl : SystemService {
    actual override val osType: OsType = OsType.Linux
    actual override val osVersion: String = ""
    actual override val osArch: OsArch = OsArch.X64
    actual override val totalPhysicalMemoryBytes: Long? = null

    actual override fun downloadsDir(): String? = null
}
