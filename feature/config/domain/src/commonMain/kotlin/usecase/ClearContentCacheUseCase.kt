package app.vercors.feature.config.domain.usecase

import app.vercors.feature.config.domain.repository.ContentCacheRepository
import dev.zacsweers.metro.Inject

@Inject
class ClearContentCacheUseCase(
    private val repository: ContentCacheRepository
) {
    suspend operator fun invoke() = repository.clear()
}
