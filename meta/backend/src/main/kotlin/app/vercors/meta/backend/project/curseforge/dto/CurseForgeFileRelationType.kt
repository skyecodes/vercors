package app.vercors.meta.backend.project.curseforge.dto

import app.vercors.meta.backend.IntEnumerable
import app.vercors.meta.backend.IntEnumerableSerializer
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
