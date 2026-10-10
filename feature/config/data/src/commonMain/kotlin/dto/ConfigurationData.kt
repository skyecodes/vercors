package app.vercors.feature.config.data.dto

import app.vercors.feature.config.domain.model.Configuration
import app.vercors.feature.config.domain.model.HomeCategoryConfig
import app.vercors.feature.config.domain.model.StartupScreen
import app.vercors.feature.config.domain.model.UpdateChannel
import app.vercors.shared.data.dto.JvmMemoryOptionsData
import app.vercors.shared.data.dto.NativeLibraryOptionsData
import app.vercors.shared.data.dto.toData
import app.vercors.shared.data.dto.toDomain
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.milliseconds

@Serializable
data class ConfigurationData(
    val metaApiUrl: String,
    val parallelDownloads: Int,
    val parallelFilesystemOperations: Int,
    val menuDrawerOpen: Boolean,
    val animations: Boolean,
    val preferredPlatform: String,
    val homeCategories: List<HomeCategoryConfigData>,
    val startupScreen: StartupScreenData = StartupScreenData.Home,
    val selectedThemeId: String,
    val selectedAccent: String,
    val preferredJavaByMajorVersion: Map<Int, String> = emptyMap(),
    val javaInstallations: List<JavaInstallationData> = emptyList(),
    val jvmMemoryOptions: JvmMemoryOptionsData = JvmMemoryOptionsData(),
    val nativeLibraryOptions: NativeLibraryOptionsData = NativeLibraryOptionsData(),
    val wrapLogLines: Boolean = true,
    val uiScalePercent: Int = 100,
    val networkTimeout: Long = 10_000,
    val horizontalTabs: Boolean = false,
    val checkForUpdatesOnStart: Boolean = true,
    val updateChannel: UpdateChannelData? = null
)

internal fun ConfigurationData.toDomain(): Configuration = Configuration(
    metaApiUrl = metaApiUrl,
    parallelDownloads = parallelDownloads,
    parallelFilesystemOperations = parallelFilesystemOperations,
    menuDrawerOpen = menuDrawerOpen,
    animations = animations,
    preferredPlatform = preferredPlatform,
    homeCategories = homeCategories.map { it.toDomain() },
    startupScreen = startupScreen.toDomain(),
    selectedThemeId = selectedThemeId,
    selectedAccent = selectedAccent,
    preferredJavaByMajorVersion = preferredJavaByMajorVersion,
    javaInstallations = javaInstallations.map { it.toDomain() },
    jvmMemoryOptions = jvmMemoryOptions.toDomain(),
    nativeLibraryOptions = nativeLibraryOptions.toDomain(),
    wrapLogLines = wrapLogLines,
    uiScalePercent = uiScalePercent,
    networkTimeout = networkTimeout.milliseconds,
    horizontalTabs = horizontalTabs,
    checkForUpdatesOnStart = checkForUpdatesOnStart,
    updateChannel = updateChannel?.toDomain()
)

internal fun Configuration.toData(): ConfigurationData = ConfigurationData(
    metaApiUrl = metaApiUrl,
    parallelDownloads = parallelDownloads,
    parallelFilesystemOperations = parallelFilesystemOperations,
    menuDrawerOpen = menuDrawerOpen,
    animations = animations,
    preferredPlatform = preferredPlatform,
    homeCategories = homeCategories.map { it.toData() },
    startupScreen = startupScreen.toData(),
    selectedThemeId = selectedThemeId,
    selectedAccent = selectedAccent,
    preferredJavaByMajorVersion = preferredJavaByMajorVersion,
    javaInstallations = javaInstallations.map { it.toData() },
    jvmMemoryOptions = jvmMemoryOptions.toData(),
    nativeLibraryOptions = nativeLibraryOptions.toData(),
    wrapLogLines = wrapLogLines,
    uiScalePercent = uiScalePercent,
    networkTimeout = networkTimeout.inWholeMilliseconds,
    horizontalTabs = horizontalTabs,
    checkForUpdatesOnStart = checkForUpdatesOnStart,
    updateChannel = updateChannel?.toData()
)

@Serializable
enum class HomeCategoryConfigData {
    @SerialName("instances")
    Instances,

    @SerialName("modpacks")
    Modpacks,

    @SerialName("mods")
    Mods,

    @SerialName("resourcepacks")
    ResourcePacks,

    @SerialName("shaderpacks")
    ShaderPacks
}

private fun HomeCategoryConfigData.toDomain(): HomeCategoryConfig = when (this) {
    HomeCategoryConfigData.Instances -> HomeCategoryConfig.Instances
    HomeCategoryConfigData.Modpacks -> HomeCategoryConfig.Modpacks
    HomeCategoryConfigData.Mods -> HomeCategoryConfig.Mods
    HomeCategoryConfigData.ResourcePacks -> HomeCategoryConfig.ResourcePacks
    HomeCategoryConfigData.ShaderPacks -> HomeCategoryConfig.ShaderPacks
}

private fun HomeCategoryConfig.toData(): HomeCategoryConfigData = when (this) {
    HomeCategoryConfig.Instances -> HomeCategoryConfigData.Instances
    HomeCategoryConfig.Modpacks -> HomeCategoryConfigData.Modpacks
    HomeCategoryConfig.Mods -> HomeCategoryConfigData.Mods
    HomeCategoryConfig.ResourcePacks -> HomeCategoryConfigData.ResourcePacks
    HomeCategoryConfig.ShaderPacks -> HomeCategoryConfigData.ShaderPacks
}

@Serializable
enum class StartupScreenData {
    @SerialName("home")
    Home,

    @SerialName("instances")
    Instances,

    @SerialName("content")
    Content,

    @SerialName("settings")
    Settings,

    @SerialName("accounts")
    Accounts,

    @SerialName("last_played_instance")
    LastPlayedInstance
}

private fun StartupScreenData.toDomain(): StartupScreen = when (this) {
    StartupScreenData.Home -> StartupScreen.Home
    StartupScreenData.Instances -> StartupScreen.Instances
    StartupScreenData.Content -> StartupScreen.Content
    StartupScreenData.Settings -> StartupScreen.Settings
    StartupScreenData.Accounts -> StartupScreen.Accounts
    StartupScreenData.LastPlayedInstance -> StartupScreen.LastPlayedInstance
}

private fun StartupScreen.toData(): StartupScreenData = when (this) {
    StartupScreen.Home -> StartupScreenData.Home
    StartupScreen.Instances -> StartupScreenData.Instances
    StartupScreen.Content -> StartupScreenData.Content
    StartupScreen.Settings -> StartupScreenData.Settings
    StartupScreen.Accounts -> StartupScreenData.Accounts
    StartupScreen.LastPlayedInstance -> StartupScreenData.LastPlayedInstance
}

@Serializable
enum class UpdateChannelData {
    @SerialName("stable")
    Stable,

    @SerialName("beta")
    Beta,

    @SerialName("alpha")
    Alpha
}

private fun UpdateChannelData.toDomain(): UpdateChannel = when (this) {
    UpdateChannelData.Stable -> UpdateChannel.Stable
    UpdateChannelData.Beta -> UpdateChannel.Beta
    UpdateChannelData.Alpha -> UpdateChannel.Alpha
}

private fun UpdateChannel.toData(): UpdateChannelData = when (this) {
    UpdateChannel.Stable -> UpdateChannelData.Stable
    UpdateChannel.Beta -> UpdateChannelData.Beta
    UpdateChannel.Alpha -> UpdateChannelData.Alpha
}
