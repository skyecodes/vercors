package app.vercors.feature.project.domain.model

import app.vercors.feature.instance.domain.model.ContentKind
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.PendingManualDownload

data class ManualDownloadCandidate(
    val fileName: String,
    val downloadUrl: String,
    val instanceId: InstanceId,
    val projectType: ProjectType,
    val size: Long?,
    val sha1: String?,
    val md5: String?,
    val platformId: String,
    val providerProjectId: String,
    val providerFileId: String,
) {
    val contentKind: ContentKind
        get() = checkNotNull(projectType.toContentKindOrNull()) {
            "Manual download is not supported for project type $projectType"
        }
}

fun ManualDownloadCandidate.toPendingManualDownload(): PendingManualDownload = PendingManualDownload(
    fileName = fileName,
    downloadUrl = downloadUrl,
    size = size,
    sha1 = sha1,
    md5 = md5,
    contentKind = contentKind,
    platformId = platformId,
    providerProjectId = providerProjectId,
    providerFileId = providerFileId,
)

fun PendingManualDownload.toManualDownloadCandidate(instanceId: InstanceId): ManualDownloadCandidate =
    ManualDownloadCandidate(
        fileName = fileName,
        downloadUrl = downloadUrl,
        instanceId = instanceId,
        projectType = contentKind.toProjectType(),
        size = size,
        sha1 = sha1,
        md5 = md5,
        platformId = platformId,
        providerProjectId = providerProjectId,
        providerFileId = providerFileId,
    )
