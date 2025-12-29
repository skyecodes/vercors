package app.vercors.meta.backend.project.curseforge.dto

import app.vercors.meta.backend.IntEnumerable
import app.vercors.meta.backend.IntEnumerableSerializer
import kotlinx.serialization.Serializable

@Serializable(CurseForgeHashAlgoSerializer::class)
enum class CurseForgeHashAlgo(override val value: Int) : IntEnumerable {
    Sha1(1),
    Md5(2)
}

internal class CurseForgeHashAlgoSerializer : IntEnumerableSerializer<CurseForgeHashAlgo>(CurseForgeHashAlgo.entries)
