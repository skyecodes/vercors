package app.vercors.feature.config.ui

import app.vercors.feature.config.domain.model.HomeCategoryConfig
import app.vercors.feature.config.domain.model.StartupScreen
import app.vercors.shared.ui.Event

sealed interface ConfigurationEvent : Event {
    data class OnTabClick(val tab: ConfigurationTab) : ConfigurationEvent

    data class OnThemeChange(val themeId: String) : ConfigurationEvent

    data class OnAccentChange(val accentId: String) : ConfigurationEvent

    data object OnAnimationsToggle : ConfigurationEvent
    data object OnHorizontalTabsToggle : ConfigurationEvent

    data class OnUiScalePercentChange(val value: Int) : ConfigurationEvent

    data class OnMetaApiUrlChange(val value: String) : ConfigurationEvent
    data class OnNetworkTimeoutChange(val value: Int) : ConfigurationEvent
    data class OnParallelDownloadsChange(val value: Int) : ConfigurationEvent
    data class OnParallelFilesystemOperationsChange(val value: Int) : ConfigurationEvent
    data class OnPreferredPlatformChange(val value: String) : ConfigurationEvent
    data class OnStartupScreenChange(val value: StartupScreen) : ConfigurationEvent
    data class OnHomeCategoriesChange(val value: List<HomeCategoryConfig>) : ConfigurationEvent

    data class OnJavaInstallationSelected(val path: String) : ConfigurationEvent

    data object OnRescanJavaClick : ConfigurationEvent

    data class OnPreferredJavaChange(val majorVersion: Int, val installationId: String) : ConfigurationEvent

    data object OnJavaAddErrorDismiss : ConfigurationEvent

    data class OnMinMemoryMbChange(val value: Int?) : ConfigurationEvent
    data class OnMaxMemoryMbChange(val value: Int?) : ConfigurationEvent
    data class OnExtraJvmArgsChange(val value: String) : ConfigurationEvent

    data class OnUseSystemGlfwToggle(val checked: Boolean) : ConfigurationEvent
    data class OnGlfwPathChange(val value: String) : ConfigurationEvent
    data class OnUseSystemOpenAlToggle(val checked: Boolean) : ConfigurationEvent
    data class OnOpenAlPathChange(val value: String) : ConfigurationEvent

    data object OnClearContentCacheClick : ConfigurationEvent
    data class OnConfirmClearContentCache(val result: Boolean) : ConfigurationEvent

    data object OnOpenDataDirectoryClick : ConfigurationEvent
    data object OnChangeDataDirectoryClick : ConfigurationEvent
    data object OnChangeDataDirectoryDismiss : ConfigurationEvent
}
