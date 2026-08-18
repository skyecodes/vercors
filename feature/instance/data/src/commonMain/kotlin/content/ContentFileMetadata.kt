package app.vercors.feature.instance.data.content

import kotlinx.serialization.Serializable

/**
 * Global, content-addressed cache entry (keyed by a file's sha1 — see [ContentDirScannerEngine]) — shared by
 * every instance that has this exact file installed, so it's parsed once regardless of how many instances or
 * filenames reference it.
 */
@Serializable
internal data class ContentFileMetadata(
    val title: String,
    val description: String?,
    val version: String?,
    val loader: String?,
    /** Icon lives at `<iconCacheDir>/<sha1>.<iconExtension>`. */
    val iconExtension: String?,
    val providers: List<ContentProviderMetadata> = emptyList()
)

@Serializable
internal data class ContentProviderMetadata(
    val platformId: String,
    val projectId: String,
    val fileId: String
)
