package app.vercors.launcher.common.data.system

import org.koin.core.annotation.Factory

@Factory
class CopyToClipboardUseCase {
    operator fun invoke(text: String): CopyToClipboardResult = copyToClipboard(text)
}

internal expect fun copyToClipboard(text: String): CopyToClipboardResult

sealed interface CopyToClipboardResult {
    data object Unsupported : CopyToClipboardResult
    data object Success : CopyToClipboardResult
    data class Failure(val error: Exception) : CopyToClipboardResult
}
