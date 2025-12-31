package app.vercors.shared.domain.usecase

import app.vercors.shared.domain.service.CopyToClipboardResult
import app.vercors.shared.domain.service.SystemService
import org.koin.core.annotation.Factory

@Factory
class CopyToClipboardUseCase(
    private val systemService: SystemService
) {
    operator fun invoke(text: String): CopyToClipboardResult = systemService.copyToClipboard(text)
}
