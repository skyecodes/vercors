package app.vercors.launcher.gui.config.data

import app.vercors.launcher.common.data.platform.PlatformTypeData
import app.vercors.launcher.common.data.platform.toData
import app.vercors.launcher.common.data.platform.toDomain
import app.vercors.launcher.gui.config.model.GuiConfiguration
import app.vercors.launcher.gui.config.model.HomeCategoryConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GuiConfigurationData(
    val menuDrawerOpen: Boolean,
    val preferredPlatform: PlatformTypeData,
    val homeCategories: List<HomeCategoryConfigData>
)

internal fun GuiConfigurationData.toDomain(): GuiConfiguration = GuiConfiguration(
    menuDrawerOpen = menuDrawerOpen,
    preferredPlatform = preferredPlatform.toDomain(),
    homeCategories = homeCategories.map { it.toDomain() }
)

internal fun GuiConfiguration.toData(): GuiConfigurationData = GuiConfigurationData(
    menuDrawerOpen = menuDrawerOpen,
    preferredPlatform = preferredPlatform.toData(),
    homeCategories = homeCategories.map { it.toData() }
)

@Serializable
internal enum class HomeCategoryConfigData {
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
