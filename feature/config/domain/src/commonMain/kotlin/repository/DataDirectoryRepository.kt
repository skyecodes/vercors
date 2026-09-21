package app.vercors.feature.config.domain.repository

import app.vercors.feature.config.domain.model.DataDirectoryError
import arrow.core.raise.Raise
import opensavvy.progress.Progress

interface DataDirectoryRepository {
    context(_: Raise<DataDirectoryError>)
    suspend fun move(newPath: String, onProgress: (Progress) -> Unit = {})
}
