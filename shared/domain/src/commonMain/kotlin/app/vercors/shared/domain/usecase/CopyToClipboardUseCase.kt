package app.vercors.shared.domain.usecase

import app.vercors.shared.domain.service.CopyToClipboardResult
import app.vercors.shared.domain.service.SystemService
import dev.zacsweers.metro.Inject

@Inject
class CopyToClipboardUseCase(
    private val systemService: SystemService
) {
    operator fun invoke(text: String): CopyToClipboardResult = systemService.copyToClipboard(text)
}
