package app.vercors.feature.config.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import app.vercors.feature.config.domain.model.HomeCategoryConfig
import app.vercors.feature.config.domain.model.StartupScreen
import app.vercors.feature.project.domain.model.ProjectPlatform
import app.vercors.shared.domain.model.JavaInstallation
import app.vercors.shared.domain.model.JvmMemoryOptions
import app.vercors.shared.domain.model.NativeLibraryOptions

@Immutable
data class ConfigurationState(
    val selectedTheme: ThemeView?,
    val themeList: List<ThemeView>,
    val selectedAccent: ThemeNamedColorView?,
    val animations: Boolean?,
    val uiScalePercent: Int?,
    val metaApiUrl: String?,
    val networkTimeoutSeconds: Int?,
    val parallelDownloads: Int?,
    val parallelFilesystemOperations: Int?,
    val preferredPlatform: String?,
    val platformList: List<ProjectPlatform>,
    val startupScreen: StartupScreen?,
    val homeCategories: List<HomeCategoryConfig>,
    val javaInstallations: List<JavaInstallation>,
    val preferredJavaByMajorVersion: Map<Int, String>,
    val javaAddError: String?,
    val javaScanInProgress: Boolean,
    val jvmMemoryOptions: JvmMemoryOptions,
    val totalMemoryMb: Int,
    val nativeLibraryOptions: NativeLibraryOptions,
    val showNativeLibraryOptions: Boolean,
    val detectedGlfwPath: String?,
    val detectedOpenAlPath: String?,
    val contentCacheSizeBytes: Long?,
    val promptClearContentCache: Boolean,
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
