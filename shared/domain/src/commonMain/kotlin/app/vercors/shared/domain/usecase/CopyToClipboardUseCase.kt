package app.vercors.shared.domain.usecase

import app.vercors.shared.system.SystemService
import dev.zacsweers.metro.Inject

@Inject
class CopyToClipboardUseCase(
    private val systemService: SystemService
) {
    operator fun invoke(text: String): Boolean = systemService.copyToClipboard(text)
}
