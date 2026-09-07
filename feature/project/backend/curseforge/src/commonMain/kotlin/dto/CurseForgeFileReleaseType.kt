package app.vercors.feature.project.backend.curseforge.dto

import kotlinx.serialization.Serializable

@Serializable(CurseForgeFileReleaseTypeSerializer::class)
enum class CurseForgeFileReleaseType(override val value: Int) : IntEnumerable {
    Release(1),
    Beta(2),
    Alpha(3)
}

internal class CurseForgeFileReleaseTypeSerializer :
    IntEnumerableSerializer<CurseForgeFileReleaseType>(CurseForgeFileReleaseType.entries)
