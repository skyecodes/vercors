package app.vercors.feature.config.domain.model

import app.vercors.shared.domain.model.JavaInstallation
import app.vercors.shared.domain.model.JvmMemoryOptions
import app.vercors.shared.domain.model.NativeLibraryOptions
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class Configuration(
    val metaApiUrl: String,
    val parallelDownloads: Int,
    val parallelFilesystemOperations: Int,
    val menuDrawerOpen: Boolean,
    val animations: Boolean,
    val preferredPlatform: String,
    val homeCategories: List<HomeCategoryConfig>,
    val startupScreen: StartupScreen,
    val selectedThemeId: String,
    val selectedAccent: String,
    val preferredJavaByMajorVersion: Map<Int, String>,
    val javaInstallations: List<JavaInstallation>,
    val jvmMemoryOptions: JvmMemoryOptions,
    val nativeLibraryOptions: NativeLibraryOptions,
    val wrapLogLines: Boolean,
    val uiScalePercent: Int,
    val networkTimeout: Duration,
    val horizontalTabs: Boolean,
    val checkForUpdatesOnStart: Boolean,
    val checkForInstanceUpdates: Boolean,
    val updateChannel: UpdateChannel?
) {
    companion object {
        const val DEFAULT_THEME_ID = "catppuccin_mocha"
        const val DEFAULT_ACCENT = "mauve"
        const val DEFAULT_UI_SCALE_PERCENT = 100

        val Default = Configuration(
            metaApiUrl = "https://meta.vercors.app",
            parallelDownloads = 10,
            parallelFilesystemOperations = 10,
            menuDrawerOpen = true,
            animations = true,
            preferredPlatform = "modrinth",
            homeCategories = HomeCategoryConfig.entries,
            startupScreen = StartupScreen.Home,
            selectedThemeId = DEFAULT_THEME_ID,
            selectedAccent = DEFAULT_ACCENT,
            preferredJavaByMajorVersion = emptyMap(),
            javaInstallations = emptyList(),
            jvmMemoryOptions = JvmMemoryOptions(),
            nativeLibraryOptions = NativeLibraryOptions(),
            wrapLogLines = true,
            uiScalePercent = DEFAULT_UI_SCALE_PERCENT,
            networkTimeout = 10.seconds,
            horizontalTabs = false,
            checkForUpdatesOnStart = true,
            checkForInstanceUpdates = true,
            updateChannel = null
        )
    }
}
