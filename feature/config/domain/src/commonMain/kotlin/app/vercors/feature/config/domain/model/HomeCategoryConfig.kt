package app.vercors.feature.config.domain.model

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
