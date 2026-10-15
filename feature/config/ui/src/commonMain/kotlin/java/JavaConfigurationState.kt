package app.vercors.feature.config.ui.java

import androidx.compose.runtime.Immutable
import app.vercors.shared.domain.model.JavaInstallation
import app.vercors.shared.domain.model.JvmMemoryOptions

@Immutable
data class JavaConfigurationState(
    val javaInstallations: List<JavaInstallation>,
    val preferredJavaByMajorVersion: Map<Int, String>,
    val javaAddError: String?,
    val javaScanInProgress: Boolean,
    val jvmMemoryOptions: JvmMemoryOptions,
    val totalMemoryMb: Int,
)
