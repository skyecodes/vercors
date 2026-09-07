package app.vercors.core.system

expect class SystemServiceImpl : SystemService {
    override val osType: OsType
    override val osVersion: String
    override val osArch: OsArch
    override val totalPhysicalMemoryBytes: Long?

    override fun downloadsDir(): String?
}
