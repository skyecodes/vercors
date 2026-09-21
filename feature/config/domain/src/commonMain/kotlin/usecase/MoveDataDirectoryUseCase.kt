package app.vercors.feature.config.domain.usecase

import app.vercors.feature.config.domain.model.DataDirectoryError
import app.vercors.feature.config.domain.repository.DataDirectoryRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject
import opensavvy.progress.Progress

@Inject
class MoveDataDirectoryUseCase(private val repository: DataDirectoryRepository) {
    context(_: Raise<DataDirectoryError>)
    suspend operator fun invoke(newPath: String, onProgress: (Progress) -> Unit = {}) =
        repository.move(newPath, onProgress)
}
