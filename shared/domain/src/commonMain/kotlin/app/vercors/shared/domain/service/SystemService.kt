package app.vercors.shared.domain.service

interface SystemService {
    val osType: OsType
    val osVersion: String
    val osArch: OsArch

    fun copyToClipboard(text: String): CopyToClipboardResult
    suspend fun openUrlInBrowser(url: String): OpenBrowserResult
}

sealed interface OpenBrowserResult {
    data object Unsupported : OpenBrowserResult
    data object Success : OpenBrowserResult
    data class Failure(val exception: Exception) : OpenBrowserResult
}

sealed interface CopyToClipboardResult {
    data object Unsupported : CopyToClipboardResult
    data object Success : CopyToClipboardResult
    data class Failure(val error: Exception) : CopyToClipboardResult
}

enum class OsType(val id: String) {
    Windows("windows"), Linux("linux"), MacOs("osx")
}

enum class OsArch(val id: String) {
    X86("x86"), X64("x64")
}
