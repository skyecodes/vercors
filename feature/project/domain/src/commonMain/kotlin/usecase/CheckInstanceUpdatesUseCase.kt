package app.vercors.feature.project.domain.usecase

import app.vercors.feature.instance.domain.model.*
import app.vercors.feature.instance.domain.repository.InstanceContentRepository
import app.vercors.feature.instance.domain.repository.InstanceRepository
import app.vercors.feature.instance.domain.usecase.UpdateInstanceUseCase
import app.vercors.feature.project.domain.model.ProjectDetailsError
import app.vercors.feature.project.domain.model.ProjectFile
import app.vercors.feature.project.domain.model.from
import app.vercors.feature.project.domain.model.update.*
import app.vercors.feature.project.domain.repository.InstanceUpdateRepository
import app.vercors.feature.project.domain.repository.ProjectRepository
import arrow.core.raise.Raise
import arrow.core.raise.context.either
import arrow.core.raise.context.withError
import arrow.core.raise.recover
import arrow.fx.coroutines.parMap
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging
import opensavvy.state.coroutines.now
import opensavvy.state.outcome.valueOrNull
import kotlin.time.Clock

private val logger = KotlinLogging.logger {}

private const val CHECK_CONCURRENCY = 8

@Inject
class CheckInstanceUpdatesUseCase(
    private val instanceRepository: InstanceRepository,
    private val projectRepository: ProjectRepository,
    private val instanceContentRepository: InstanceContentRepository,
    private val instanceUpdateRepository: InstanceUpdateRepository,
    private val identifyInstalledContent: IdentifyInstalledContentUseCase,
    private val updateInstance: UpdateInstanceUseCase,
) {
    context(_: Raise<InstanceUpdateError>)
    suspend operator fun invoke(instanceId: InstanceId, identifyUnknown: Boolean): InstanceUpdates {
        instanceUpdateRepository.setChecking(instanceId, true)
        try {
            return check(instanceId, identifyUnknown)
        } finally {
            instanceUpdateRepository.setChecking(instanceId, false)
        }
    }

    context(_: Raise<InstanceUpdateError>)
    private suspend fun check(instanceId: InstanceId, identifyUnknown: Boolean): InstanceUpdates {
        val instance = withError({ _: InstanceError -> InstanceUpdateError.InstanceNotFound }) {
            instanceRepository.findInstance(instanceId)
        }
        val modpackInfo = instance.modpackInfo
        val updates = if (modpackInfo != null) {
            InstanceUpdates(Clock.System.now(), modpack = checkModpack(instance.id, modpackInfo), content = emptyList())
        } else {
            if (identifyUnknown) identifyInstalledContent(instance.id)
            InstanceUpdates(Clock.System.now(), modpack = null, content = checkContent(instance))
        }
        instanceUpdateRepository.set(instance.id, updates)
        return updates
    }

    context(_: Raise<InstanceUpdateError>)
    private suspend fun checkModpack(instanceId: InstanceId, modpackInfo: InstanceModpackInfo): ModpackUpdate? =
        withError(InstanceUpdateError::ModpackLookupFailed) {
            val projectId = modpackInfo.projectId from modpackInfo.platformId
            val files = projectRepository.getProjectFiles(projectId, gameVersion = null, loader = null)
            val current = files.find { it.id == modpackInfo.fileId }
                ?: projectRepository.getProjectFile(projectId, modpackInfo.fileId)
            if (modpackInfo.versionName == null) saveVersionName(instanceId, modpackInfo, current.displayName)
            files.findUpdateFor(current)?.let { ModpackUpdate(projectId, current, it) }
        }

    // Instances installed before the version name was stored
    private suspend fun saveVersionName(instanceId: InstanceId, modpackInfo: InstanceModpackInfo, versionName: String) {
        either {
            updateInstance(instanceId) { instance ->
                instance.takeIf { it.modpackInfo == modpackInfo }
                    ?.copy(modpackInfo = modpackInfo.copy(versionName = versionName))
                    ?: instance
            }
        }.onLeft { logger.warn { "Couldn't save the modpack version name of $instanceId: $it" } }
    }

    private suspend fun checkContent(instance: Instance): List<ContentUpdate> {
        val tracked = ContentKind.entries.flatMap { kind ->
            val installed = instanceContentRepository.observeInstalledContent(kind, instance.id).now().valueOrNull
            installed.orEmpty().mapNotNull { file -> file.preferredProvider()?.let { TrackedContent(kind, file, it) } }
        }
        val currentFiles = tracked.groupBy { it.provider.platformId }
            .flatMap { (platformId, entries) -> fetchCurrentFiles(platformId, entries) }
            .associateBy { it.platformId to it.file.id }
        return tracked.parMap(concurrency = CHECK_CONCURRENCY) { entry ->
            val current = currentFiles[entry.provider.platformId to entry.provider.fileId]?.file
                ?: return@parMap null
            findContentUpdate(instance, entry, current)
        }.filterNotNull()
    }

    private suspend fun fetchCurrentFiles(platformId: String, entries: List<TrackedContent>): List<PlatformFile> =
        recover({
            projectRepository.getProjectFilesByIds(platformId, entries.map { it.provider.fileId }.distinct())
                .map { PlatformFile(platformId, it) }
        }) { error: ProjectDetailsError ->
            logger.warn { "Couldn't fetch installed $platformId files for an update check: $error" }
            emptyList()
        }

    private suspend fun findContentUpdate(
        instance: Instance,
        entry: TrackedContent,
        current: ProjectFile
    ): ContentUpdate? {
        val projectId = entry.provider.projectId from entry.provider.platformId
        val files = recover({
            projectRepository.getProjectFiles(projectId, instance.gameVersion, loader = null)
        }) { error: ProjectDetailsError ->
            logger.warn { "Couldn't fetch files of $projectId for an update check: $error" }
            return null
        }
        val loader = instance.modLoader?.type
        val compatible = if (entry.kind == ContentKind.Mod && loader != null) {
            files.filter { it.runsOn(loader) }
        } else {
            files
        }
        val latest = compatible.findUpdateFor(current) ?: return null
        return ContentUpdate(entry.kind, entry.file.fileName, entry.file.enabled, projectId, current, latest)
    }

    private data class TrackedContent(val kind: ContentKind, val file: InstalledContentFile, val provider: ModProvider)

    private data class PlatformFile(val platformId: String, val file: ProjectFile)
}

// Modrinth first: its files never need a manual download
private fun InstalledContentFile.preferredProvider(): ModProvider? =
    providers.find { it.platformId == MODRINTH_PLATFORM_ID } ?: providers.firstOrNull()
