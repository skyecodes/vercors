package app.vercors.feature.java.domain.repository

import app.vercors.core.domain.ProgressiveStateFlow
import app.vercors.feature.java.domain.model.JavaInstallationError
import app.vercors.shared.domain.model.JavaInstallation
import arrow.core.raise.Raise

interface JavaRepository {
    val installations: ProgressiveStateFlow<Nothing, List<JavaInstallation>>

    suspend fun rescan()

    context(_: Raise<JavaInstallationError>)
    suspend fun addInstallation(path: String): JavaInstallation
}
