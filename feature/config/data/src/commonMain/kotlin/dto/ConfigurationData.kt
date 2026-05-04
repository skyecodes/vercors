package app.vercors.feature.config.data.dto

import app.vercors.feature.config.domain.model.Configuration
import app.vercors.feature.config.domain.model.HomeCategoryConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConfigurationData(
    val metaApiUrl: String,
    val parallelDownloads: Int,
    val parallelFilesystemOperations: Int,
    val menuDrawerOpen: Boolean,
    val animations: Boolean,
    val preferredPlatform: String,
    val homeCategories: List<HomeCategoryConfigData>,
    val selectedThemeId: String,
    val selectedAccent: String
)

internal fun ConfigurationData.toDomain(): Configuration = Configuration(
    metaApiUrl = metaApiUrl,
    parallelDownloads = parallelDownloads,
    parallelFilesystemOperations = parallelFilesystemOperations,
    menuDrawerOpen = menuDrawerOpen,
    animations = animations,
    preferredPlatform = preferredPlatform,
    homeCategories = homeCategories.map { it.toDomain() },
    selectedThemeId = selectedThemeId,
    selectedAccent = selectedAccent,
)

internal fun Configuration.toData(): ConfigurationData = ConfigurationData(
    metaApiUrl = metaApiUrl,
    parallelDownloads = parallelDownloads,
    parallelFilesystemOperations = parallelFilesystemOperations,
    menuDrawerOpen = menuDrawerOpen,
    animations = animations,
    preferredPlatform = preferredPlatform,
    homeCategories = homeCategories.map { it.toData() },
    selectedThemeId = selectedThemeId,
    selectedAccent = selectedAccent,
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
