package app.vercors.feature.instance.domain.model

import app.vercors.feature.game.domain.model.ModLoaderType
import kotlin.time.Instant

/** A parsed, installed content file (mod, resource pack, or shader pack) belonging to an instance. */
data class InstalledContentFile(
    val fileName: String,
    val title: String,
    val description: String?,
    /** Null for resource packs/shader packs — neither format has a version concept. */
    val version: String?,
    /** Always null for resource packs/shader packs — neither is loader-specific. */
    val loader: ModLoaderType?,
    val fileSize: Long,
    val iconFilePath: String?,
    val providers: List<ModProvider>,
    val enabled: Boolean,
    /** The file's on-disk last-modified time, or `null` if the platform couldn't report one. */
    val lastModified: Instant?,
)
