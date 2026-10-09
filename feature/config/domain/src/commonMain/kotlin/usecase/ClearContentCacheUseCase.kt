package app.vercors.feature.config.domain.usecase

import app.vercors.feature.config.domain.repository.ContentCacheRepository
import dev.zacsweers.metro.Inject

@Inject
class ClearContentCacheUseCase(
    private val contentCacheRepository: ContentCacheRepository
) {
    suspend operator fun invoke(): Unit = contentCacheRepository.clear()
}
