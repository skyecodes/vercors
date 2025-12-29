package app.vercors.meta.backend.project.curseforge.dto

import app.vercors.meta.backend.IntEnumerable
import app.vercors.meta.backend.IntEnumerableSerializer
import kotlinx.serialization.Serializable

@Serializable(CurseForgeFileReleaseTypeSerializer::class)
enum class CurseForgeFileReleaseType(override val value: Int) : IntEnumerable {
    Release(1),
    Beta(2),
    Alpha(3)
}

internal class CurseForgeFileReleaseTypeSerializer :
    IntEnumerableSerializer<CurseForgeFileReleaseType>(CurseForgeFileReleaseType.entries)
