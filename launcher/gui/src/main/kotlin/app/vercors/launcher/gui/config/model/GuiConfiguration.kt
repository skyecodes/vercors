package app.vercors.launcher.gui.config.model

import app.vercors.shared.domain.project.model.ProjectPlatform

data class GuiConfiguration(
    val menuDrawerOpen: Boolean = true,
    val preferredPlatform: ProjectPlatform = ProjectPlatform.Modrinth,
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
