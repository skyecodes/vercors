package app.vercors.feature.instance.ui.details.content

import androidx.compose.runtime.Immutable
import app.vercors.feature.instance.domain.model.InstalledContentFile
import app.vercors.feature.instance.domain.model.ModProvider
import kotlin.time.Instant

@Immutable
data class InstalledContentState(
    val files: List<InstalledContentFileView>?,
    val promptRemoveConfirmation: PendingContentRemoval?,
)

@Immutable
data class PendingContentRemoval(
    val fileName: String,
    val title: String,
)

@Immutable
data class InstalledContentFileView(
    val fileName: String,
    val title: String,
    val description: String?,
    val version: String?,
    val iconFilePath: String?,
    val enabled: Boolean,
    val providers: List<ModProvider>,
    val lastModified: Instant?,
)

fun InstalledContentFile.toView(): InstalledContentFileView = InstalledContentFileView(
    fileName = fileName,
    title = title,
    description = description,
    version = version,
    iconFilePath = iconFilePath,
    enabled = enabled,
    providers = providers,
    lastModified = lastModified
)
