package app.vercors.feature.project.backend.curseforge.dto

import kotlinx.serialization.Serializable

@Serializable(CurseForgeProjectSearchSortFieldSerializer::class)
enum class CurseForgeProjectSearchSortField(override val value: Int) : IntEnumerable {
    Featured(1),
    Popularity(2),
    LastUpdated(3),
    Name(4),
    Author(5),
    TotalDownloads(6),
    Category(7),
    GameVersion(8),
    EarlyAccess(9),
    FeaturedReleased(10),
    ReleasedDate(11),
    Rating(12)
}

internal class CurseForgeProjectSearchSortFieldSerializer :
    IntEnumerableSerializer<CurseForgeProjectSearchSortField>(CurseForgeProjectSearchSortField.entries)
