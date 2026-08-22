package app.vercors.feature.config.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import app.vercors.shared.domain.model.JavaInstallation
import app.vercors.shared.domain.model.JvmMemoryOptions

@Immutable
data class ConfigurationState(
    val selectedTheme: ThemeView?,
    val themeList: List<ThemeView>,
    val selectedAccent: ThemeNamedColorView?,
    val animations: Boolean?,
    val javaInstallations: List<JavaInstallation>,
    val preferredJavaByMajorVersion: Map<Int, String>,
    val javaAddError: String?,
    val javaScanInProgress: Boolean,
    val jvmMemoryOptions: JvmMemoryOptions,
    val totalMemoryMb: Int,
)

@Immutable
data class ThemeView(
    val id: String,
    val displayName: String,
    val accents: List<ThemeNamedColorView>,
    val defaultAccent: ThemeNamedColorView,
)

@Immutable
data class ThemeNamedColorView(
    val id: String,
    val name: String,
    val color: Color,
)
