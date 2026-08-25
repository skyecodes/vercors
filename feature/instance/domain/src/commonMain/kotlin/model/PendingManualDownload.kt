package app.vercors.feature.instance.domain.model

import app.vercors.feature.instance.domain.usecase.ContentKind

/**
 * A modpack file requiring a manual download that hasn't been placed into the instance yet, persisted on
 * [Instance.pendingManualDownloads] so a launcher restart doesn't lose track of it. Carries everything needed to
 * redisplay it and to fully resume detection/placement/validation/provenance-recording from scratch.
 */
data class PendingManualDownload(
    val fileName: String,
    val downloadUrl: String,
    val size: Long?,
    val sha1: String?,
    val md5: String?,
    val contentKind: ContentKind,
    val platformId: String,
    val providerProjectId: String,
    val providerFileId: String,
)
