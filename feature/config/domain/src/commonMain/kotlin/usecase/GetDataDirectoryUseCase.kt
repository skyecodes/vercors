package app.vercors.feature.config.domain.usecase

import app.vercors.feature.config.domain.repository.DataDirectoryRepository
import dev.zacsweers.metro.Inject

@Inject
class GetDataDirectoryUseCase(
    private val dataDirectoryRepository: DataDirectoryRepository
) {
    operator fun invoke(): String = dataDirectoryRepository.currentPath
}
