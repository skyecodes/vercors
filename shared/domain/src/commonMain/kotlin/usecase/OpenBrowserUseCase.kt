package app.vercors.shared.domain.usecase

import app.vercors.core.system.SystemService
import dev.zacsweers.metro.Inject

@Inject
class OpenBrowserUseCase(
    private val systemService: SystemService
) {
    suspend operator fun invoke(url: String): Boolean = systemService.openUrlInBrowser(url)
}
