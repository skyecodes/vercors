package app.vercors.feature.project.backend.curseforge.dto

import kotlinx.serialization.Serializable

@Serializable(CurseForgeFileRelationTypeSerializer::class)
enum class CurseForgeFileRelationType(override val value: Int) : IntEnumerable {
    EmbeddedLibrary(1),
    OptionalDependency(2),
    RequiredDependency(3),
    Tool(4),
    Incompatible(5),
    Include(6)
}

internal class CurseForgeFileRelationTypeSerializer :
    IntEnumerableSerializer<CurseForgeFileRelationType>(CurseForgeFileRelationType.entries)
