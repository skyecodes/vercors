package app.vercors.feature.config.ui.nativelibraries

import androidx.compose.runtime.Immutable
import app.vercors.shared.domain.model.NativeLibraryOptions

@Immutable
data class NativeLibraryConfigurationState(
    val supported: Boolean,
    val nativeLibraryOptions: NativeLibraryOptions,
    val detectedGlfwPath: String?,
    val detectedOpenAlPath: String?,
)
