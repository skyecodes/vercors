package app.vercors.feature.config.gui.domain.model

data class GuiConfiguration(
    val menuDrawerOpen: Boolean = true,
    val animations: Boolean = true,
    val preferredPlatform: String = "modrinth",
    val homeCategories: List<HomeCategoryConfig> = HomeCategoryConfig.entries
)

enum class HomeCategoryConfig(val type: Type) {
    Instances(Type.Instance),
    Modpacks(Type.Project),
    Mods(Type.Project),
    ResourcePacks(Type.Project),
    ShaderPacks(Type.Project);

    enum class Type {
        Instance, Project
    }
}
