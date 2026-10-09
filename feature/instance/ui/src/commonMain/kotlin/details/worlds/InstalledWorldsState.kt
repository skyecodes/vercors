package app.vercors.feature.instance.ui.details.worlds

import androidx.compose.runtime.Immutable
import app.vercors.feature.instance.domain.model.InstalledWorld
import app.vercors.feature.instance.ui.details.stripMinecraftFormattingForDisplay
import nl.jacobras.humanreadable.HumanReadable
import kotlin.time.Instant

@Immutable
data class InstalledWorldsState(
    val worlds: List<InstalledWorldView>?,
    val promptRemoveConfirmation: PendingWorldRemoval?,
)

@Immutable
data class PendingWorldRemoval(val folderName: String)

@Immutable
data class InstalledWorldView(
    val folderName: String,
    val displayName: String,
    val iconFilePath: String?,
    val lastModified: Instant?,
) {
    val lastModifiedReadable = lastModified?.let { HumanReadable.timeAgo(it) }
}

fun InstalledWorld.toView(): InstalledWorldView = InstalledWorldView(
    folderName = folderName,
    displayName = folderName.stripMinecraftFormattingForDisplay(),
    iconFilePath = iconFilePath,
    lastModified = lastModified
)
