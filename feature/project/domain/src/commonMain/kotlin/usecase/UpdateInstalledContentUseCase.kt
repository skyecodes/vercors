package app.vercors.feature.project.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceContentRepository
import app.vercors.feature.project.domain.model.*
import app.vercors.feature.project.domain.model.update.ContentUpdate
import app.vercors.feature.project.domain.model.update.InstanceUpdateError
import app.vercors.feature.project.domain.repository.InstanceUpdateRepository
import app.vercors.feature.project.domain.repository.ProjectRepository
import arrow.core.raise.Raise
import arrow.core.raise.context.withError
import dev.zacsweers.metro.Inject
import opensavvy.progress.Progress

@Inject
class UpdateInstalledContentUseCase(
    private val projectRepository: ProjectRepository,
    private val instanceContentRepository: InstanceContentRepository,
    private val instanceUpdateRepository: InstanceUpdateRepository,
    private val runManualDownloads: RunManualDownloadsUseCase,
) {
    context(_: Raise<InstanceUpdateError>)
    suspend operator fun invoke(
        instanceId: InstanceId,
        updates: List<ContentUpdate>,
        onManualDownloadError: suspend (ManualDownloadError) -> Unit,
        onProgress: (Progress) -> Unit = {}
    ): Boolean {
        val (automatic, manual) = updates.partition { it.latest.manualDownloadUrl == null }
        if (automatic.isNotEmpty()) {
            withError({ _: ProjectFileDownloadError -> InstanceUpdateError.DownloadFailed }) {
                projectRepository.downloadProjectFiles(
                    automatic.map { it.toDownloadCandidate() },
                    instanceId,
                    onProgress
                )
            }
            automatic.forEach { replace(instanceId, it) }
        }
        if (manual.isEmpty()) return true
        val manualOk = runManualDownloads(
            manual.map { it.toManualDownloadCandidate(instanceId) },
            onManualDownloadError
        ) {}
        // Partial results can't be told apart, so old files stay until every manual download succeeded
        if (manualOk) manual.forEach { replace(instanceId, it) }
        return manualOk
    }

    // The origin is recorded before disabling so it follows the rename
    private suspend fun replace(instanceId: InstanceId, update: ContentUpdate) {
        val newFileName = update.latest.fileName
        instanceContentRepository.recordOrigin(
            update.kind,
            instanceId,
            newFileName,
            update.projectId.platform,
            update.projectId.id,
            update.latest.id
        )
        if (newFileName != update.fileName) instanceContentRepository.remove(update.kind, instanceId, update.fileName)
        if (!update.enabled) instanceContentRepository.setEnabled(update.kind, instanceId, newFileName, enabled = false)
        instanceUpdateRepository.removeContent(instanceId, setOf(update.fileName))
    }
}

private fun ContentUpdate.toDownloadCandidate(): ProjectFileDownloadCandidate = ProjectFileDownloadCandidate(
    projectId = projectId,
    fileId = latest.id,
    fileName = latest.fileName,
    type = kind.toProjectType(),
    size = latest.size,
    sha1 = latest.sha1,
    md5 = latest.md5
)

private fun ContentUpdate.toManualDownloadCandidate(instanceId: InstanceId): ManualDownloadCandidate =
    ManualDownloadCandidate(
        fileName = latest.fileName,
        downloadUrl = requireNotNull(latest.manualDownloadUrl),
        instanceId = instanceId,
        projectType = kind.toProjectType(),
        size = latest.size,
        sha1 = latest.sha1,
        md5 = latest.md5,
        platformId = projectId.platform,
        providerProjectId = projectId.id,
        providerFileId = latest.id,
    )
