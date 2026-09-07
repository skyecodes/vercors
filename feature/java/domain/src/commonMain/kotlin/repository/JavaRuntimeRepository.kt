package app.vercors.feature.java.domain.repository

import app.vercors.feature.java.domain.model.JavaRuntimeError
import app.vercors.feature.java.domain.model.JavaRuntimeInstallStatus
import app.vercors.shared.domain.model.JavaInstallation
import arrow.core.raise.Raise
import kotlinx.coroutines.flow.StateFlow

interface JavaRuntimeRepository {
    /** In-progress installs, keyed by the major version being installed. */
    val installProgress: StateFlow<Map<Int, JavaRuntimeInstallStatus>>

    /** Downloads and installs a Java runtime matching [majorVersion] for the current OS/arch from Mojang's runtime
     *  API, probes it, and registers it as a new [JavaInstallation]. */
    context(_: Raise<JavaRuntimeError>)
    suspend fun installRuntime(majorVersion: Int): JavaInstallation
}
