package app.vercors.feature.project.domain.usecase

import app.vercors.core.domain.toDownloadProgressOrNull
import app.vercors.core.domain.toFractionOrNull
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.ManualDownloadItem
import app.vercors.feature.instance.domain.usecase.RecordInstalledContentOriginUseCase
import app.vercors.feature.project.domain.model.*
import app.vercors.feature.project.domain.repository.ManualDownloadRepository
import app.vercors.feature.project.domain.repository.ProjectRepository
import arrow.core.raise.recover
import dev.zacsweers.metro.Inject

@Inject
class InstallProjectFilesUseCase(
    private val projectRepository: ProjectRepository,
    private val manualDownloadRepository: ManualDownloadRepository,
    private val runManualDownloads: RunManualDownloadsUseCase,
    private val recordInstalledContentOrigin: RecordInstalledContentOriginUseCase,
) {
    suspend operator fun invoke(
        instanceId: InstanceId,
        project: ProjectInstallCandidate,
        dependencies: List<ProjectInstallCandidate>,
        onDownloadError: suspend (ProjectFileDownloadError) -> Unit,
        onManualDownloadError: suspend (ManualDownloadError) -> Unit,
        onManualDownloadItemsChanged: (List<ManualDownloadItem>) -> Unit,
        onProgress: (ProjectInstallProgress) -> Unit,
    ): Boolean = if (project.type == ProjectType.World) {
        installWorld(
            instanceId,
            project,
            onDownloadError,
            onManualDownloadError,
            onManualDownloadItemsChanged,
            onProgress
        )
    } else {
        installFiles(
            instanceId,
            listOf(project) + dependencies,
            onDownloadError,
            onManualDownloadError,
            onManualDownloadItemsChanged,
            onProgress
        )
    }

    private suspend fun installFiles(
        instanceId: InstanceId,
        candidates: List<ProjectInstallCandidate>,
        onDownloadError: suspend (ProjectFileDownloadError) -> Unit,
        onManualDownloadError: suspend (ManualDownloadError) -> Unit,
        onManualDownloadItemsChanged: (List<ManualDownloadItem>) -> Unit,
        onProgress: (ProjectInstallProgress) -> Unit,
    ): Boolean {
        val (autoCandidates, manualCandidates) = candidates.partition { it.manualDownloadUrl == null }
        val totalCount = candidates.size

        val autoOk = recover({
            if (autoCandidates.isNotEmpty()) {
                projectRepository.downloadProjectFiles(autoCandidates.map { it.toDownloadCandidate() }, instanceId) {
                    onProgress(
                        ProjectInstallProgress(
                            fraction = (it.toFractionOrNull() ?: 0f) * autoCandidates.size / totalCount,
                            download = it.toDownloadProgressOrNull(),
                        )
                    )
                }
                autoCandidates.forEach { candidate ->
                    recordInstalledContentOrigin(
                        requireNotNull(candidate.type.toContentKindOrNull()),
                        instanceId,
                        candidate.fileName,
                        candidate.projectId.platform,
                        candidate.projectId.id,
                        candidate.fileId
                    )
                }
            }
            onProgress(ProjectInstallProgress(autoCandidates.size.toFloat() / totalCount))
            true
        }) { error: ProjectFileDownloadError ->
            onDownloadError(error)
            false
        }

        return autoOk && runManualDownloads(
            manualCandidates.map { it.toManualDownloadCandidate(instanceId) },
            onError = onManualDownloadError,
        ) { items ->
            onManualDownloadItemsChanged(items)
            val doneCount = autoCandidates.size + items.count { it.downloaded }
            onProgress(ProjectInstallProgress(doneCount.toFloat() / totalCount))
        }
    }

    private suspend fun installWorld(
        instanceId: InstanceId,
        candidate: ProjectInstallCandidate,
        onDownloadError: suspend (ProjectFileDownloadError) -> Unit,
        onManualDownloadError: suspend (ManualDownloadError) -> Unit,
        onManualDownloadItemsChanged: (List<ManualDownloadItem>) -> Unit,
        onProgress: (ProjectInstallProgress) -> Unit,
    ): Boolean {
        val manualDownloadUrl = candidate.manualDownloadUrl
        val ok = if (manualDownloadUrl == null) {
            recover({
                projectRepository.installWorldFile(candidate.toDownloadCandidate(), instanceId) {
                    onProgress(ProjectInstallProgress(it.toFractionOrNull() ?: 0f, it.toDownloadProgressOrNull()))
                }
                true
            }) { error: ProjectFileDownloadError ->
                onDownloadError(error)
                false
            }
        } else {
            onManualDownloadItemsChanged(
                listOf(ManualDownloadItem(candidate.fileName, manualDownloadUrl, downloaded = false))
            )
            recover({
                manualDownloadRepository.awaitManualWorldDownload(
                    candidate.fileName,
                    instanceId,
                    candidate.size,
                    candidate.sha1,
                    candidate.md5
                )
                true
            }) { error: ManualDownloadError ->
                onManualDownloadError(error)
                false
            }
        }
        onManualDownloadItemsChanged(emptyList())
        onProgress(ProjectInstallProgress(1f))
        return ok
    }
}

private fun ProjectInstallCandidate.toDownloadCandidate(): ProjectFileDownloadCandidate =
    ProjectFileDownloadCandidate(projectId, fileId, fileName, type, size, sha1, md5)

private fun ProjectInstallCandidate.toManualDownloadCandidate(instanceId: InstanceId): ManualDownloadCandidate =
    ManualDownloadCandidate(
        fileName = fileName,
        downloadUrl = requireNotNull(manualDownloadUrl),
        instanceId = instanceId,
        projectType = type,
        size = size,
        sha1 = sha1,
        md5 = md5,
        platformId = projectId.platform,
        providerProjectId = projectId.id,
        providerFileId = fileId,
    )
