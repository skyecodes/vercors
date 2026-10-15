package app.vercors.feature.project.domain.usecase

import app.vercors.core.domain.toFractionOrNull
import app.vercors.feature.instance.domain.model.*
import app.vercors.feature.instance.domain.repository.InstanceContentRepository
import app.vercors.feature.instance.domain.repository.InstanceRepository
import app.vercors.feature.instance.domain.usecase.UpdateInstanceStatusUseCase
import app.vercors.feature.instance.domain.usecase.UpdateInstanceUseCase
import app.vercors.feature.project.domain.model.ManualDownloadError
import app.vercors.feature.project.domain.model.modpack.ModpackManifest
import app.vercors.feature.project.domain.model.modpack.ModpackManifestFile
import app.vercors.feature.project.domain.model.modpack.ModpackResolutionError
import app.vercors.feature.project.domain.model.pack.InstalledPack
import app.vercors.feature.project.domain.model.pack.planPackFileReplacement
import app.vercors.feature.project.domain.model.toContentKindOrNull
import app.vercors.feature.project.domain.model.update.InstanceUpdateError
import app.vercors.feature.project.domain.model.update.ModpackUpdate
import app.vercors.feature.project.domain.repository.InstalledPackRepository
import app.vercors.feature.project.domain.repository.InstanceUpdateRepository
import app.vercors.feature.project.domain.repository.ModpackRepository
import arrow.core.raise.Raise
import arrow.core.raise.context.either
import arrow.core.raise.context.raise
import arrow.core.raise.context.withError
import arrow.core.raise.recover
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging
import opensavvy.state.coroutines.now
import opensavvy.state.outcome.valueOrNull
import kotlin.time.Clock

private val logger = KotlinLogging.logger {}

@Inject
class UpdateModpackUseCase(
    private val instanceRepository: InstanceRepository,
    private val getModpackManifest: GetModpackManifestUseCase,
    private val modpackRepository: ModpackRepository,
    private val runModpackInstall: RunModpackInstallUseCase,
    private val updateInstance: UpdateInstanceUseCase,
    private val updateInstanceStatus: UpdateInstanceStatusUseCase,
    private val instanceContentRepository: InstanceContentRepository,
    private val instanceUpdateRepository: InstanceUpdateRepository,
    private val installedPackRepository: InstalledPackRepository,
    private val removeInstalledPackFiles: RemoveInstalledPackFilesUseCase,
) {
    context(_: Raise<InstanceUpdateError>)
    suspend operator fun invoke(
        instanceId: InstanceId,
        update: ModpackUpdate,
        onManualDownloadError: suspend (ManualDownloadError) -> Unit
    ): Boolean {
        val instance = withError({ _: InstanceError -> InstanceUpdateError.InstanceNotFound }) {
            instanceRepository.findInstance(instanceId)
        }
        val modpackInfo = instance.modpackInfo ?: raise(InstanceUpdateError.InstanceNotFound)
        updateInstanceStatus(instance.id, InstanceStatus.Installing.ResolvingFiles(progress = null))
        val newManifest = recover({
            modpackRepository.resolveModpackManifestFiles(
                getModpackManifest(
                    update.projectId,
                    update.latest.id
                )
            ) { progress ->
                updateInstanceStatus(instance.id, InstanceStatus.Installing.ResolvingFiles(progress.toFractionOrNull()))
            }
        }) { error: ModpackResolutionError ->
            updateInstanceStatus(instance.id, InstanceStatus.Idle)
            raise(InstanceUpdateError.ManifestFailed(error))
        }
        val oldManifest = recover({
            modpackRepository.resolveModpackManifestFiles(getModpackManifest(update.projectId, modpackInfo.fileId))
        }) { error: ModpackResolutionError ->
            logger.warn { "Couldn't fetch the previous manifest of ${instance.id}, keeping its old files: $error" }
            null
        }
        val oldPack = installedPackRepository.get(instance.id)
        val disabled = oldManifest?.let { removeReplacedFiles(instance, it, newManifest) }.orEmpty()
        val newModpackInfo = InstanceModpackInfo(
            newManifest.platformId,
            newManifest.projectId,
            newManifest.fileId,
            update.latest.displayName
        )
        saveNewVersion(instance, newManifest, newModpackInfo)
        instanceUpdateRepository.clear(instance.id)

        val ok = runModpackInstall(instance.id, newModpackInfo, newManifest, onManualDownloadError)
        restoreDisabledFiles(instance, newManifest, disabled)
        removeStaleOverrides(instance.id, oldPack)
        return ok
    }

    // The manifest diff can't see files the old pack shipped through its overrides
    private suspend fun removeStaleOverrides(instanceId: InstanceId, oldPack: InstalledPack?) {
        val newPack = installedPackRepository.get(instanceId) ?: return
        if (oldPack == null || oldPack == newPack) return
        removeInstalledPackFiles(instanceId, planPackFileReplacement(oldPack, newPack, emptySet()).removed)
    }

    private suspend fun removeReplacedFiles(
        instance: Instance,
        oldManifest: ModpackManifest,
        newManifest: ModpackManifest
    ): List<ModpackManifestFile> {
        val enabledByKind = ContentKind.entries.associateWith { kind ->
            instanceContentRepository.observeInstalledContent(kind, instance.id).now().valueOrNull.orEmpty()
                .associate { it.fileName to it.enabled }
        }
        val plan = planModpackFileReplacement(oldManifest, newManifest, enabledByKind)
        plan.removed.forEach { (kind, fileName) -> instanceContentRepository.remove(kind, instance.id, fileName) }
        return plan.disabled
    }

    context(_: Raise<InstanceUpdateError>)
    private suspend fun saveNewVersion(
        instance: Instance,
        manifest: ModpackManifest,
        modpackInfo: InstanceModpackInfo
    ) {
        either {
            updateInstance(instance.id) {
                it.copy(
                    gameVersion = manifest.gameVersion,
                    modLoader = manifest.loaderType?.let { type ->
                        InstanceModLoader(type, requireNotNull(manifest.loaderVersion))
                    },
                    modpackInfo = modpackInfo,
                    installStage = InstanceInstallStage.ResolvingFiles,
                    updatedAt = Clock.System.now()
                )
            }
        }.onLeft { error ->
            logger.error { "Couldn't save the new modpack version of ${instance.id}: $error" }
            updateInstanceStatus(instance.id, InstanceStatus.Idle)
            raise(InstanceUpdateError.SaveFailed)
        }
    }

    private suspend fun restoreDisabledFiles(
        instance: Instance,
        newManifest: ModpackManifest,
        disabled: List<ModpackManifestFile>
    ) {
        for (file in newManifest.filesToDisable(disabled)) {
            val kind = file.kind.toContentKindOrNull() ?: continue
            instanceContentRepository.setEnabled(kind, instance.id, file.fileName, enabled = false)
        }
    }
}

internal data class ModpackFileReplacement(
    val removed: List<Pair<ContentKind, String>>,
    val disabled: List<ModpackManifestFile>
)

// Disabled files are removed too, or the new enabled copy would land next to them
internal fun planModpackFileReplacement(
    oldManifest: ModpackManifest,
    newManifest: ModpackManifest,
    enabledByKind: Map<ContentKind, Map<String, Boolean>>
): ModpackFileReplacement {
    val newPaths = newManifest.files.mapTo(HashSet()) { it.targetPath }
    val removed = mutableListOf<Pair<ContentKind, String>>()
    val disabled = mutableListOf<ModpackManifestFile>()
    for (file in oldManifest.files) {
        val kind = file.kind.toContentKindOrNull() ?: continue
        val enabled = enabledByKind[kind]?.get(file.fileName) ?: continue
        if (!enabled) disabled += file
        if (!enabled || file.targetPath !in newPaths) removed += kind to file.fileName
    }
    return ModpackFileReplacement(removed, disabled)
}

internal fun ModpackManifest.filesToDisable(disabled: List<ModpackManifestFile>): List<ModpackManifestFile> {
    val disabledProjects = disabled.mapNotNullTo(HashSet()) { it.providerProjectId }
    val disabledNames = disabled.mapTo(HashSet()) { it.fileName }
    return files.filter { file ->
        file.kind.toContentKindOrNull() != null &&
                (file.providerProjectId in disabledProjects || file.fileName in disabledNames)
    }
}

internal val ModpackManifestFile.fileName: String
    get() = targetPath.substringAfterLast('/')
