package app.vercors.meta.backend.project.curseforge.dto

import app.vercors.meta.backend.IntEnumerable
import app.vercors.meta.backend.IntEnumerableSerializer
import kotlinx.serialization.Serializable

@Serializable(CurseForgeModStatusSerializer::class)
enum class CurseForgeProjectStatus(override val value: Int) : IntEnumerable {
    New(1),
    ChangesRequired(2),
    UnderSoftReview(3),
    Approved(4),
    Rejected(5),
    ChangesMade(6),
    Inactive(7),
    Abandoned(8),
    Deleted(9),
    UnderReview(10)
}

private class CurseForgeModStatusSerializer :
    IntEnumerableSerializer<CurseForgeProjectStatus>(CurseForgeProjectStatus.entries)