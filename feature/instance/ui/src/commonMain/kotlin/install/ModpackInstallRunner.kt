package app.vercors.feature.instance.ui.install

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.InstanceInstallStage
import app.vercors.feature.instance.domain.model.InstanceModpackInfo
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.domain.usecase.*
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.domain.model.modpack.*
import app.vercors.feature.project.domain.usecase.AwaitManualDownloadUseCase
import app.vercors.feature.project.domain.usecase.GetModpackManifestUseCase
import app.vercors.feature.project.domain.usecase.InstallModpackFilesUseCase
import app.vercors.feature.project.domain.usecase.ResolveModpackManifestFilesUseCase
import app.vercors.shared.ui.SnackbarService
import arrow.core.raise.context.either
import arrow.core.raise.recover
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging
import opensavvy.progress.Progress

private val logger = KotlinLogging.logger {}

/**
 * Runs the shared, resumable portion of the modpack-install pipeline for an already-created instance: resolve the
 * manifest's files → download them → record provenance → await any manual downloads. Shared between the
 * fresh-install path (`InstallModpackPresenter.confirmInstall`, which already has the [ModpackManifest] in hand and
 * passes it as `initialManifest` to avoid a redundant refetch) and the resume path (`InstanceDetailsPresenter`,
 * which only has the instance's persisted [InstanceModpackInfo] and passes `initialManifest = null` to have it
 * refetched — cheap, since [GetModpackManifestUseCase] routes through the same skip-if-already-valid
 * `FileValidationService.submit`).
 *
 * Resuming always restarts the whole pipeline from the top rather than tracking granular per-file progress; this is
 * safe/cheap for the same reason (every download call skips files that already validate).
 */
@Inject
class ModpackInstallRunner(
    private val getModpackManifest: GetModpackManifestUseCase,
    private val resolveModpackManifestFiles: ResolveModpackManifestFilesUseCase,
    private val installModpackFiles: InstallModpackFilesUseCase,
    private val awaitManualDownload: AwaitManualDownloadUseCase,
    private val recordInstalledContentOrigin: RecordInstalledContentOriginUseCase,
    private val updateInstance: UpdateInstanceUseCase,
    private val updateInstanceStatus: UpdateInstanceStatusUseCase,
    private val updateManualDownloadItems: UpdateManualDownloadItemsUseCase,
    private val snackbarService: SnackbarService,
) {
    /**
     * Returns `true` once every step succeeded and all manual downloads (if any) completed — the caller should then
     * set [InstanceStatus.Idle]. Returns `false` on any failure — the caller should then set
     * [InstanceStatus.Failed.InstallFailed].
     */
    suspend fun run(
        instanceId: InstanceId,
        modpackInfo: InstanceModpackInfo,
        initialManifest: ModpackManifest?
    ): Boolean {
        // Immediate live-status feedback the instant this starts — matters most for a resume, where the manifest
        // refetch below is a real network call with no progress callback of its own; without this the "Resume
        // Installation" button would look inert for however long that takes.
        updateInstanceStatus(instanceId, InstanceStatus.Installing.ResolvingFiles(progress = null))
        persistInstallStage(instanceId, InstanceInstallStage.ResolvingFiles)

        val manifest = initialManifest ?: fetchManifestOrNull(instanceId, modpackInfo) ?: return false
        val resolvedManifest = resolveManifestFilesOrNull(manifest, instanceId) ?: return false
        persistInstallStage(instanceId, InstanceInstallStage.DownloadingFiles)
        val installedFiles = installFilesOrNull(resolvedManifest, instanceId) ?: return false
        recordOrigins(instanceId, resolvedManifest.platformId, installedFiles)
        persistInstallStage(instanceId, null)
        return awaitManualDownloads(resolvedManifest, instanceId)
    }

    private suspend fun fetchManifestOrNull(
        instanceId: InstanceId,
        modpackInfo: InstanceModpackInfo
    ): ModpackManifest? =
        recover({
            getModpackManifest(
                ProjectId(id = modpackInfo.projectId, platform = modpackInfo.platformId),
                modpackInfo.fileId
            )
        }) { error: ModpackResolutionError ->
            logger.error { "Failed to re-fetch modpack manifest for $instanceId: $error" }
            updateInstanceStatus(instanceId, InstanceStatus.Failed.InstallFailed)
            null
        }

    private suspend fun resolveManifestFilesOrNull(
        manifest: ModpackManifest,
        instanceId: InstanceId
    ): ModpackManifest? =
        recover({
            resolveModpackManifestFiles(manifest) { progress ->
                updateInstanceStatus(instanceId, InstanceStatus.Installing.ResolvingFiles(progress.toFractionOrNull()))
            }
        }) { error: ModpackResolutionError ->
            logger.error { "Modpack file resolution failed for $instanceId: $error" }
            updateInstanceStatus(instanceId, InstanceStatus.Failed.InstallFailed)
            null
        }

    private suspend fun installFilesOrNull(
        resolvedManifest: ModpackManifest,
        instanceId: InstanceId
    ): List<InstalledModpackFile>? =
        recover({
            installModpackFiles(resolvedManifest, instanceId) { progress ->
                updateInstanceStatus(
                    instanceId,
                    InstanceStatus.Installing.DownloadingFiles(progress.toFractionOrNull())
                )
            }
        }) { error: ModpackInstallError ->
            logger.error { "Modpack install failed for $instanceId: $error" }
            updateInstanceStatus(instanceId, InstanceStatus.Failed.InstallFailed)
            null
        }

    private suspend fun recordOrigins(
        instanceId: InstanceId,
        platformId: String,
        installedFiles: List<InstalledModpackFile>
    ) {
        for (installedFile in installedFiles) {
            val kind = installedFile.kind.toContentKindOrNull() ?: continue
            val providerProjectId = installedFile.providerProjectId ?: continue
            val providerFileId = installedFile.providerFileId ?: continue
            recordInstalledContentOrigin(
                kind,
                instanceId,
                installedFile.fileName,
                platformId,
                providerProjectId,
                providerFileId
            )
        }
    }

    private suspend fun awaitManualDownloads(resolvedManifest: ModpackManifest, instanceId: InstanceId): Boolean {
        updateInstanceStatus(instanceId, InstanceStatus.Installing.AwaitingManualDownloads)
        val manualCandidates = buildManualDownloadCandidates(resolvedManifest, instanceId)
        return runManualDownloads(
            manualCandidates, awaitManualDownload, recordInstalledContentOrigin, snackbarService
        ) { items ->
            updateManualDownloadItems(instanceId, items)
            persistPendingManualDownloads(instanceId, manualCandidates, items, updateInstance)
        }
    }

    private fun buildManualDownloadCandidates(
        resolvedManifest: ModpackManifest,
        instanceId: InstanceId
    ): List<ManualDownloadCandidate> = resolvedManifest.files.mapNotNull { manualFile ->
        val manualDownloadUrl = manualFile.manualDownloadUrl ?: return@mapNotNull null
        val providerProjectId = manualFile.providerProjectId ?: return@mapNotNull null
        val providerFileId = manualFile.providerFileId ?: return@mapNotNull null
        val fileName = manualFile.targetPath.substringAfterLast('/')
        ManualDownloadCandidate(
            fileName = fileName,
            downloadUrl = manualDownloadUrl,
            instanceId = instanceId,
            projectType = ProjectType.Mod,
            size = manualFile.size,
            sha1 = manualFile.sha1,
            md5 = manualFile.md5,
            platformId = resolvedManifest.platformId,
            providerProjectId = providerProjectId,
            providerFileId = providerFileId,
        )
    }

    private suspend fun persistInstallStage(instanceId: InstanceId, stage: InstanceInstallStage?) {
        either { updateInstance(instanceId) { it.copy(installStage = stage) } }
            .onLeft { error -> logger.warn { "Failed to persist install stage $stage for $instanceId: $error" } }
    }
}

private fun Progress.toFractionOrNull(): Float? = when (this) {
    is Progress.Loading.Quantified -> normalized.toFloat()
    Progress.Done -> 1f
    Progress.Loading.Unquantified -> null
}

private fun ModpackFileKind.toContentKindOrNull(): ContentKind? = when (this) {
    ModpackFileKind.Mod -> ContentKind.Mod
    ModpackFileKind.ResourcePack -> ContentKind.ResourcePack
    ModpackFileKind.ShaderPack -> ContentKind.ShaderPack
    ModpackFileKind.Other -> null
}
