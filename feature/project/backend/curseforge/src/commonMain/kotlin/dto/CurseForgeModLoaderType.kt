package app.vercors.feature.project.backend.curseforge.dto

import kotlinx.serialization.Serializable

@Serializable(CurseForgeModLoaderTypeSerializer::class)
enum class CurseForgeModLoaderType(override val value: Int) : IntEnumerable {
    Any(0),
    Forge(1),
    Cauldron(2),
    LiteLoader(3),
    Fabric(4),
    Quilt(5),
    NeoForge(6)
}

internal class CurseForgeModLoaderTypeSerializer :
    IntEnumerableSerializer<CurseForgeModLoaderType>(CurseForgeModLoaderType.entries)
