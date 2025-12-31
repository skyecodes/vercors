package app.vercors.shared.domain.usecase

import app.vercors.shared.domain.service.OpenBrowserResult
import app.vercors.shared.domain.service.SystemService
import org.koin.core.annotation.Factory

@Factory
class OpenBrowserUseCase(
    private val systemService: SystemService
) {
    suspend operator fun invoke(url: String): OpenBrowserResult = systemService.openUrlInBrowser(url)
}
