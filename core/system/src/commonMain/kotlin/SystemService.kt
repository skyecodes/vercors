package app.vercors.core.system

interface SystemService {
    val osType: OsType
    val osVersion: String
    val osArch: OsArch
    val canCopyToClipboard: Boolean
    val canOpenUrlInBrowser: Boolean
    val canOpenFileManager: Boolean

    /** Total installed physical RAM, in bytes, or `null` if it can't be determined. */
    val totalPhysicalMemoryBytes: Long?

    fun copyToClipboard(text: String): Boolean

    /** The OS "Downloads" folder for the current user, or null if it can't be resolved. */
    fun downloadsDir(): String?

    /** Opens [path] (a file or directory) in the OS's file manager. */
    fun openInFileManager(path: String): Boolean
}

enum class OsType(val id: String) {
    Windows("windows"), Linux("linux"), MacOs("osx")
}

enum class OsArch(val id: String) {
    X86("x86"), X64("x64")
}
