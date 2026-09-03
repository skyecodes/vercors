package app.vercors.feature.config.ui

import app.vercors.feature.config.domain.model.HomeCategoryConfig
import app.vercors.feature.config.domain.model.StartupScreen
import app.vercors.shared.ui.Event

sealed interface ConfigurationEvent : Event {
    @JvmInline
    value class SelectTheme(val themeId: String) : ConfigurationEvent

    @JvmInline
    value class SelectAccent(val accentId: String) : ConfigurationEvent

    data object ToggleAnimations : ConfigurationEvent

    data class SetUiScalePercent(val value: Int) : ConfigurationEvent

    data class SetMetaApiUrl(val value: String) : ConfigurationEvent
    data class SetNetworkTimeout(val value: Int) : ConfigurationEvent
    data class SetParallelDownloads(val value: Int) : ConfigurationEvent
    data class SetParallelFilesystemOperations(val value: Int) : ConfigurationEvent
    data class SetPreferredPlatform(val value: String) : ConfigurationEvent
    data class SetStartupScreen(val value: StartupScreen) : ConfigurationEvent
    data class SetHomeCategories(val value: List<HomeCategoryConfig>) : ConfigurationEvent

    @JvmInline
    value class AddJavaInstallation(val path: String) : ConfigurationEvent

    data object RescanJavaInstallations : ConfigurationEvent

    data class SetPreferredJava(val majorVersion: Int, val installationId: String) : ConfigurationEvent

    data object DismissJavaAddError : ConfigurationEvent

    data class SetMinMemoryMb(val value: Int?) : ConfigurationEvent
    data class SetMaxMemoryMb(val value: Int?) : ConfigurationEvent
    data class SetExtraJvmArgs(val value: String) : ConfigurationEvent

    data class ToggleUseSystemGlfw(val checked: Boolean) : ConfigurationEvent
    data class SetGlfwPath(val value: String) : ConfigurationEvent
    data class ToggleUseSystemOpenAl(val checked: Boolean) : ConfigurationEvent
    data class SetOpenAlPath(val value: String) : ConfigurationEvent

    data object PromptClearContentCache : ConfigurationEvent
    data class OnConfirmClearContentCache(val result: Boolean) : ConfigurationEvent
}
