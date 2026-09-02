package app.vercors.feature.config.domain.usecase

import app.vercors.feature.config.domain.repository.ContentCacheRepository
import dev.zacsweers.metro.Inject

@Inject
class GetContentCacheSizeUseCase(
    private val repository: ContentCacheRepository
) {
    suspend operator fun invoke(): Long = repository.sizeBytes()
}
