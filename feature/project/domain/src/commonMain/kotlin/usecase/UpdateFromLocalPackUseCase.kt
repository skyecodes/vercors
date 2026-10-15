package app.vercors.feature.project.domain.usecase

import app.vercors.feature.instance.domain.model.*
import app.vercors.feature.instance.domain.repository.InstanceContentRepository
import app.vercors.feature.instance.domain.repository.InstanceRepository
import app.vercors.feature.instance.domain.usecase.UpdateInstanceStatusUseCase
import app.vercors.feature.instance.domain.usecase.UpdateInstanceUseCase
import app.vercors.feature.project.domain.model.pack.*
import app.vercors.feature.project.domain.repository.InstalledPackRepository
import app.vercors.feature.project.domain.repository.InstanceUpdateRepository
import arrow.core.raise.Raise
import arrow.core.raise.context.either
import arrow.core.raise.context.raise
import arrow.core.raise.context.withError
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging
import opensavvy.state.coroutines.now
import opensavvy.state.outcome.valueOrNull
import kotlin.time.Clock

private val logger = KotlinLogging.logger {}

// Like a local import, an interrupted update has no resume path
@Inject
class UpdateFromLocalPackUseCase(
    private val instanceRepository: InstanceRepository,
    private val installedPackRepository: InstalledPackRepository,
    private val instanceContentRepository: InstanceContentRepository,
    private val instanceUpdateRepository: InstanceUpdateRepository,
    private val installLocalPack: InstallLocalPackUseCase,
    private val removeInstalledPackFiles: RemoveInstalledPackFilesUseCase,
    private val updateInstance: UpdateInstanceUseCase,
    private val updateInstanceStatus: UpdateInstanceStatusUseCase,
) {
    context(_: Raise<LocalPackError>)
    suspend operator fun invoke(instanceId: InstanceId, pack: LocalPack) {
        withError({ _: InstanceError -> LocalPackError.InstanceNotFound }) {
            instanceRepository.findInstance(instanceId)
        }
        updateInstanceStatus(instanceId, InstanceStatus.Installing.ResolvingFiles(progress = null))
        val oldPack = installedPackRepository.get(instanceId)
        val disabledOldPaths = disabledPaths(instanceId, oldPack)
        saveNewVersion(instanceId, pack)

        val result = installLocalPack(pack, instanceId, isUpdate = true)
        val replacement = planPackFileReplacement(oldPack, result.installedPack, disabledOldPaths)
        removeInstalledPackFiles(instanceId, replacement.removed)
        replacement.toDisable.forEach { instanceContentRepository.setEnabled(it.kind, instanceId, it.fileName, false) }
        instanceUpdateRepository.clear(instanceId)
    }

    // Refreshed first: the scanner state is empty for an instance nothing has observed yet this session
    private suspend fun disabledPaths(instanceId: InstanceId, oldPack: InstalledPack?): Set<String> {
        val oldContent = oldPack?.files.orEmpty()
            .mapNotNull { file -> packContentFileOrNull(file.path)?.let { file.path to it } }
        if (oldContent.isEmpty()) return emptySet()
        val enabledByKind = ContentKind.entries.associateWith { kind ->
            instanceContentRepository.refresh(kind, instanceId)
            instanceContentRepository.observeInstalledContent(kind, instanceId).now().valueOrNull.orEmpty()
                .associate { it.fileName to it.enabled }
        }
        return oldContent
            .filter { (_, content) -> enabledByKind[content.kind]?.get(content.fileName) == false }
            .mapTo(HashSet()) { (path, _) -> path }
    }

    context(_: Raise<LocalPackError>)
    private suspend fun saveNewVersion(instanceId: InstanceId, pack: LocalPack) {
        either {
            updateInstance(instanceId) {
                it.copy(
                    gameVersion = pack.gameVersion,
                    modLoader = pack.loaderType?.let { type ->
                        InstanceModLoader(type, requireNotNull(pack.loaderVersion))
                    },
                    modpackInfo = pack.modpackInfo,
                    updatedAt = Clock.System.now()
                )
            }
        }.onLeft { error ->
            logger.error { "Couldn't save the new pack version of $instanceId: $error" }
            raise(LocalPackError.InstallFailed)
        }
    }
}
