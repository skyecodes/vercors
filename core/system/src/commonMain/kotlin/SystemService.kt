package app.vercors.shared.system

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlin.coroutines.CoroutineContext

interface SystemService {
    val osType: OsType
    val osVersion: String
    val osArch: OsArch
    val canCopyToClipboard: Boolean
    val canOpenUrlInBrowser: Boolean

    fun copyToClipboard(text: String): Boolean
    suspend fun openUrlInBrowser(url: String, context: CoroutineContext = Dispatchers.IO): Boolean
}

enum class OsType(val id: String) {
    Windows("windows"), Linux("linux"), MacOs("osx")
}

enum class OsArch(val id: String) {
    X86("x86"), X64("x64")
}
