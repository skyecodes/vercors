package app.vercors.feature.instance.data.repository

import app.vercors.core.domain.ProgressiveStateFlow
import app.vercors.core.domain.progressiveStateIn
import app.vercors.core.filesystem.*
import app.vercors.core.system.SystemService
import app.vercors.feature.instance.data.datasource.InstanceDataSource
import app.vercors.feature.instance.data.dto.toData
import app.vercors.feature.instance.data.dto.toDomain
import app.vercors.feature.instance.domain.model.*
import app.vercors.feature.instance.domain.repository.InstanceRepository
import app.vercors.shared.data.AppPaths
import app.vercors.shared.data.FileValidationFailure
import app.vercors.shared.data.FileValidationRequest
import app.vercors.shared.data.FileValidationService
import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.Raise
import arrow.core.raise.context.bind
import arrow.core.raise.context.ensure
import arrow.core.raise.context.ensureNotNull
import arrow.core.raise.recover
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.io.files.Path
import opensavvy.state.coroutines.now
import opensavvy.state.outcome.value
import opensavvy.state.progressive.map
import opensavvy.state.progressive.valueOrNull
import kotlin.time.Clock

private val logger = KotlinLogging.logger {}
private const val DEFAULT_ICON_EXTENSION = "png"

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class InstanceRepositoryImpl(
    coroutineScope: CoroutineScope,
    private val dataSource: InstanceDataSource,
    private val appPaths: AppPaths,
    private val fileValidationService: FileValidationService,
    private val systemService: SystemService,
) : InstanceRepository {
    override val instances: ProgressiveStateFlow<Nothing, Map<InstanceId, Either<InstanceLoadingError, Instance>>> =
        dataSource.flow.map { outcome ->
            outcome.map { map ->
                map.mapValues { (id, either) ->
                    either.mapLeft { InstanceLoadingError(id, it) }.map { it.toDomain(id, appPaths) }
                }
            }
        }.progressiveStateIn(coroutineScope)

    private val _statuses = MutableStateFlow<Map<InstanceId, InstanceStatus>>(emptyMap())
    override val statuses = _statuses.asStateFlow()

    private val _jobs = MutableStateFlow<Map<InstanceId, Job>>(emptyMap())

    private val _logs = MutableStateFlow<Map<InstanceId, List<InstanceLogEvent>>>(emptyMap())
    override val logs = _logs.asStateFlow()

    private val _manualDownloads = MutableStateFlow<Map<InstanceId, List<ManualDownloadItem>>>(emptyMap())
    override val manualDownloads = _manualDownloads.asStateFlow()

    init {
        // Restores AwaitingManualDownloads (from pendingManualDownloads) or Installing.Interrupted (from
        // installStage) status from disk for any instance still mid-install when the launcher was last closed —
        // otherwise it silently defaults to Idle (see getInstanceStatus) and looks fully installed/launchable.
        // pendingManualDownloads takes priority: it reflects a strictly later stage than installStage, which is
        // always cleared to null before pendingManualDownloads is ever populated (see ModpackInstallRunner.run).
        // Skips instances that already have a live status entry so it never clobbers an in-progress session's own
        // updates once its write round-trips back here.
        coroutineScope.launch {
            instances.collect { outcome ->
                val map = outcome.valueOrNull ?: return@collect
                for ((id, either) in map) {
                    val instance = either.getOrNull() ?: continue
                    if (_statuses.value.containsKey(id)) continue
                    val stage = instance.installStage
                    when {
                        instance.pendingManualDownloads.isNotEmpty() -> {
                            _statuses.update { it + (id to InstanceStatus.Installing.AwaitingManualDownloads) }
                            _manualDownloads.update {
                                it + (id to instance.pendingManualDownloads.map { pending ->
                                    ManualDownloadItem(pending.fileName, pending.downloadUrl, downloaded = false)
                                })
                            }
                        }

                        stage != null -> _statuses.update { it + (id to InstanceStatus.Installing.Interrupted(stage)) }
                    }
                }
            }
        }
    }

    context(_: Raise<InstanceValidationError>)
    override suspend fun saveInstance(instance: Instance) {
        ensure(instance.id.isNotEmpty()) { InstanceValidationError.EmptyInstanceId }
        ensure(instance.name.length >= 3) { InstanceValidationError.NameTooShort }
        val instanceData = instance.toData()
        dataSource.write(instance.id, instanceData)
        logger.info { "Saved instance ${instance.id}" }
    }

    override suspend fun deleteInstance(instanceId: InstanceId) {
        dataSource.delete(instanceId)
        logger.info { "Deleted instance $instanceId" }
    }

    override suspend fun deleteInstanceFiles(instanceId: InstanceId) {
        val gameDir = appPaths.gameDir(instanceId)
        if (gameDir.exists()) gameDir.deleteRecursively()
        logger.info { "Deleted game directory for instance $instanceId" }
    }

    context(_: Raise<InstanceError>)
    override suspend fun findInstance(id: InstanceId): Instance =
        ensureNotNull(instances.now().value[id]?.bind()) { InstanceNotFound(id) }

    context(_: Raise<InstanceError>)
    override suspend fun setInstanceIcon(instanceId: InstanceId, sourcePath: String) {
        val instance = findInstance(instanceId)
        val extension = sourcePath.substringAfterLast('.', DEFAULT_ICON_EXTENSION)
        val targetPath = appPaths.instanceIconFile(instanceId, extension)
        Path(sourcePath).copyTo(targetPath)
        deletePreviousIconIfDifferent(instance.iconFilePath, targetPath)
        dataSource.write(
            instanceId,
            instance.copy(iconFilePath = targetPath.toString(), updatedAt = Clock.System.now()).toData()
        )
    }

    override suspend fun setInstanceIconFromUrl(instance: Instance, url: String): Boolean {
        val instanceId = instance.id
        val extension = url.substringBefore('?').substringAfterLast('.', DEFAULT_ICON_EXTENSION)
        val targetPath = appPaths.instanceIconFile(instanceId, extension)
        val downloaded = recover({
            fileValidationService.submit(listOf(FileValidationRequest.Url(url, targetPath))) {}
            true
        }) { failures: NonEmptyList<FileValidationFailure> ->
            logger.warn { "Failed to download icon from $url for instance $instanceId: $failures" }
            false
        }
        if (!downloaded) return false
        deletePreviousIconIfDifferent(instance.iconFilePath, targetPath)
        dataSource.write(
            instanceId,
            instance.copy(iconFilePath = targetPath.toString(), updatedAt = Clock.System.now()).toData()
        )
        return true
    }

    context(_: Raise<InstanceError>)
    override suspend fun removeInstanceIcon(instanceId: InstanceId) {
        val instance = findInstance(instanceId)
        instance.iconFilePath?.let { Path(it) }?.let { if (it.exists()) it.delete() }
        dataSource.write(instanceId, instance.copy(iconFilePath = null, updatedAt = Clock.System.now()).toData())
    }

    override suspend fun openInstanceFolder(instanceId: InstanceId, folder: InstanceFolder): Boolean {
        val dir = when (folder) {
            InstanceFolder.Root -> appPaths.gameDir(instanceId)
            InstanceFolder.Mods -> appPaths.modsDir(instanceId)
            InstanceFolder.ResourcePacks -> appPaths.resourcePacksDir(instanceId)
            InstanceFolder.ShaderPacks -> appPaths.shaderPacksDir(instanceId)
            InstanceFolder.Config -> appPaths.configDir(instanceId)
        }
        if (!dir.exists()) dir.createDirectories()
        return systemService.openInFileManager(dir.toString())
    }

    /**
     * Deletes the previously-stored icon file, unless it's the exact file we just wrote the new icon to (same
     * extension as before — [FileValidationRequest]/[copyTo] already overwrote it in place).
     */
    private suspend fun deletePreviousIconIfDifferent(previousIconFilePath: String?, newTargetPath: Path) {
        if (previousIconFilePath == null || previousIconFilePath == newTargetPath.toString()) return
        val oldPath = Path(previousIconFilePath)
        if (oldPath.exists()) oldPath.delete()
    }

    override fun updateInstanceStatus(id: InstanceId, status: InstanceStatus) {
        _statuses.update { it + (id to status) }
    }

    override fun getInstanceStatus(id: InstanceId): InstanceStatus {
        return _statuses.value.getOrElse(id) { InstanceStatus.Idle }
    }

    override fun updateInstanceJob(id: InstanceId, job: Job?) {
        if (job != null) _jobs.update { it + (id to job) }
        else _jobs.update { it - id }
        logger.debug { "Updated instance job $id" }
    }

    override fun cancelInstanceJob(id: InstanceId) {
        _jobs.value[id]?.cancel()
    }

    override fun hasActiveJob(id: InstanceId): Boolean = _jobs.value.containsKey(id)

    override suspend fun stopInstance(id: InstanceId) {
        _statuses.value[id]?.let { status ->
            if (status is InstanceStatus.Running) status.stop()
        }
    }

    override suspend fun killInstance(id: InstanceId) {
        _statuses.value[id]?.let { status ->
            if (status is InstanceStatus.Stopping) status.kill()
        }
    }

    override fun addInstanceLog(id: InstanceId, logEvent: InstanceLogEvent) {
        _logs.update { it + (id to it.getOrElse(id, ::emptyList) + logEvent) }
    }

    override fun clearInstanceLogs(id: InstanceId) {
        _logs.update { it - id }
    }

    override fun updateManualDownloadItems(id: InstanceId, items: List<ManualDownloadItem>) {
        if (items.isEmpty()) _manualDownloads.update { it - id }
        else _manualDownloads.update { it + (id to items) }
    }
}
