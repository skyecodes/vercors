package app.vercors.feature.instance.ui.install

import app.vercors.core.domain.toFractionOrNull
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.InstanceInstallStage
import app.vercors.feature.instance.domain.model.InstanceModpackInfo
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.domain.usecase.RecordInstalledContentOriginUseCase
import app.vercors.feature.instance.domain.usecase.UpdateInstanceStatusUseCase
import app.vercors.feature.instance.domain.usecase.UpdateInstanceUseCase
import app.vercors.feature.instance.domain.usecase.UpdateManualDownloadItemsUseCase
import app.vercors.feature.instance.ui.InstanceRes
import app.vercors.feature.instance.ui.notificationInstallFailedMessage
import app.vercors.feature.instance.ui.notificationInstallFinishedMessage
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
import dev.nucleusframework.notification.common.notification
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.compose.resources.getString

private val logger = KotlinLogging.logger {}

suspend fun notifyModpackInstallResult(instanceName: String, ok: Boolean) {
    notification(
        title = instanceName,
        message = getString(
            if (ok) {
                InstanceRes.string.notificationInstallFinishedMessage
            } else {
                InstanceRes.string.notificationInstallFailedMessage
            }
        ),
    ).send()
}

// Resuming always restarts the whole pipeline: every download skips files that already validate
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
    suspend fun run(
        instanceId: InstanceId,
        modpackInfo: InstanceModpackInfo,
        initialManifest: ModpackManifest?
    ): Boolean {
        // Immediate feedback: a resume's manifest refetch has no progress callback of its own
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
            projectType = manualFile.kind.toProjectType(),
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

private fun ModpackFileKind.toProjectType(): ProjectType = when (this) {
    ModpackFileKind.Mod, ModpackFileKind.Other -> ProjectType.Mod
    ModpackFileKind.ResourcePack -> ProjectType.ResourcePack
    ModpackFileKind.ShaderPack -> ProjectType.ShaderPack
}
