package app.vercors.shared.domain.project.model

enum class ProjectType(val requiresLoader: Boolean) {
    Mod(requiresLoader = true),
    Modpack(requiresLoader = true),
    ResourcePack(requiresLoader = false),
    ShaderPack(requiresLoader = false),
    DataPack(requiresLoader = false),
    World(requiresLoader = false),
}
