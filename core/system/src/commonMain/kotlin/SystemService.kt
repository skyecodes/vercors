package app.vercors.core.system

interface SystemService {
    val osType: OsType
    val osVersion: String
    val osArch: OsArch
    val canCopyToClipboard: Boolean get() = false
    val canOpenUrlInBrowser: Boolean get() = false
    val canOpenFileManager: Boolean get() = false

    val totalPhysicalMemoryBytes: Long?

    fun copyToClipboard(text: String): Boolean = false

    fun downloadsDir(): String?

    fun openInFileManager(path: String): Boolean = false
}

enum class OsType(val id: String) {
    Windows("windows"), Linux("linux"), MacOs("osx")
}

enum class OsArch(val id: String) {
    X86("x86"), X64("x64"), Arm64("arm64")
}
