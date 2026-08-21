package app.vercors.feature.instance.data.content

import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.instance.domain.model.ModProvider
import kotlin.time.Instant

internal data class ContentEntry(
    val fileName: String,
    val title: String,
    val description: String?,
    val version: String?,
    val loader: ModLoaderType?,
    val fileSize: Long,
    val iconFilePath: String?,
    val providers: List<ModProvider>,
    val enabled: Boolean,
    val lastModified: Instant?
)
