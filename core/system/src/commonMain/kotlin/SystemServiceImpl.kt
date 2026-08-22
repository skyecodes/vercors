package app.vercors.core.system

expect class SystemServiceImpl : SystemService {
    override val osType: OsType
    override val osVersion: String
    override val osArch: OsArch
    override val canCopyToClipboard: Boolean
    override val canOpenUrlInBrowser: Boolean
    override val canOpenFileManager: Boolean
    override val totalPhysicalMemoryBytes: Long?

    override fun copyToClipboard(text: String): Boolean
    override fun downloadsDir(): String?
    override fun openInFileManager(path: String): Boolean
}
