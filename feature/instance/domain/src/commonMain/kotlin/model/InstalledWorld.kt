package app.vercors.feature.instance.domain.model

import kotlin.time.Instant

data class InstalledWorld(
    val folderName: String,
    val iconFilePath: String?,
    val iconLastModified: Instant?,
    val lastModified: Instant?
)
