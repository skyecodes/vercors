package app.vercors.feature.project.backend.curseforge.dto

import kotlinx.serialization.Serializable

@Serializable(CurseForgeHashAlgoSerializer::class)
enum class CurseForgeHashAlgo(override val value: Int) : IntEnumerable {
    Sha1(1),
    Md5(2)
}

internal class CurseForgeHashAlgoSerializer : IntEnumerableSerializer<CurseForgeHashAlgo>(CurseForgeHashAlgo.entries)
