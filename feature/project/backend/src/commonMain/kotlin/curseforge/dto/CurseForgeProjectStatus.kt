package app.vercors.feature.project.backend.curseforge.dto

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

internal class CurseForgeModStatusSerializer :
    IntEnumerableSerializer<CurseForgeProjectStatus>(CurseForgeProjectStatus.entries)
