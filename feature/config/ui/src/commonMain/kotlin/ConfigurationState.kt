package app.vercors.feature.config.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import app.vercors.feature.config.domain.model.HomeCategoryConfig
import app.vercors.feature.config.domain.model.LauncherUpdateState
import app.vercors.feature.config.domain.model.StartupScreen
import app.vercors.feature.config.domain.model.UpdateChannel
import app.vercors.feature.config.ui.datadirectory.ChangeDataDirectoryDialog
import app.vercors.feature.project.domain.model.ProjectPlatform
import app.vercors.shared.domain.model.JavaInstallation
import app.vercors.shared.domain.model.JvmMemoryOptions
import app.vercors.shared.domain.model.NativeLibraryOptions

@Immutable
data class ConfigurationState(
    val selectedTab: ConfigurationTab,
    val selectedTheme: ThemeView?,
    val themeList: List<ThemeView>,
    val selectedAccent: ThemeNamedColorView?,
    val animations: Boolean?,
    val uiScalePercent: Int?,
    val horizontalTabs: Boolean?,
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
    val dataDirectory: String,
    val canChangeDataDirectory: Boolean,
    val promptChangeDataDirectory: Boolean,
    val changeDataDirectoryDialog: ChangeDataDirectoryDialog,
    val appName: String,
    val appVersion: String,
    val checkForUpdatesOnStart: Boolean?,
    val updateChannel: UpdateChannel?,
    val launcherUpdate: LauncherUpdateState,
    val promptRestartForUpdate: Boolean,
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
