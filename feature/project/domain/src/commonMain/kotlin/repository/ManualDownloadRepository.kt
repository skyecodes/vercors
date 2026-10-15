package app.vercors.feature.project.domain.repository

import app.vercors.feature.project.domain.model.ManualDownloadError
import app.vercors.feature.project.domain.model.ProjectType
import arrow.core.raise.Raise

interface ManualDownloadRepository {
    suspend fun installManualDownloadLocally(
        fileName: String,
        instanceId: String,
        type: ProjectType,
        size: Long?,
        sha1: String?,
        md5: String?
    ): Boolean

    context(_: Raise<ManualDownloadError>)
    suspend fun awaitManualDownload(
        fileName: String,
        instanceId: String,
        type: ProjectType,
        size: Long?,
        sha1: String?,
        md5: String?
    )

    context(_: Raise<ManualDownloadError>)
    suspend fun awaitManualWorldDownload(
        fileName: String,
        instanceId: String,
        size: Long?,
        sha1: String?,
        md5: String?
    )
}
