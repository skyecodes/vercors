package app.vercors.feature.java.domain.repository

import app.vercors.feature.java.domain.model.JavaRuntimeError
import app.vercors.feature.java.domain.model.JavaRuntimeInstallStatus
import app.vercors.shared.domain.model.JavaInstallation
import arrow.core.raise.Raise
import kotlinx.coroutines.flow.StateFlow

interface JavaRuntimeRepository {
    val installProgress: StateFlow<Map<Int, JavaRuntimeInstallStatus>>

    context(_: Raise<JavaRuntimeError>)
    suspend fun installRuntime(majorVersion: Int): JavaInstallation
}
