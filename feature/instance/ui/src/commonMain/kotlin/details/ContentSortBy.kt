package app.vercors.feature.instance.ui.details

import androidx.compose.runtime.Composable
import app.vercors.feature.instance.ui.InstanceRes
import app.vercors.feature.instance.ui.sortByLastModified
import app.vercors.feature.instance.ui.sortByName
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

enum class ContentSortBy {
    Name,
    LastModified
}

val ContentSortBy.displayName: String
    @Composable get() = stringResource(
        when (this) {
            ContentSortBy.Name -> InstanceRes.string.sortByName
            ContentSortBy.LastModified -> InstanceRes.string.sortByLastModified
        }
    )

fun <T> List<T>.filteredAndSorted(
    searchQuery: String,
    sortBy: ContentSortBy,
    name: (T) -> String,
    lastModified: (T) -> Instant?,
): List<T> {
    val filtered = filter { searchQuery.isBlank() || name(it).contains(searchQuery, ignoreCase = true) }
    return when (sortBy) {
        ContentSortBy.Name -> filtered.sortedBy { name(it).lowercase() }
        ContentSortBy.LastModified -> filtered.sortedByDescending { lastModified(it) ?: Instant.DISTANT_PAST }
    }
}
