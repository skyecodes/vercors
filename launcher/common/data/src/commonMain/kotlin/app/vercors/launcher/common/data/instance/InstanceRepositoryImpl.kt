package app.vercors.launcher.common.data.instance

import app.vercors.launcher.common.data.IODispatcher
import app.vercors.launcher.common.data.deleteRecursively
import app.vercors.launcher.common.domain.instance.Instance
import app.vercors.launcher.common.domain.instance.InstanceRepository
import app.vercors.launcher.common.domain.settings.GlobalSettings
import app.vercors.launcher.common.domain.settings.SettingsRepository
import app.vercors.shared.domain.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource
import kotlinx.serialization.json.io.encodeToSink
import org.koin.core.annotation.Single

val logger = KotlinLogging.logger {}

internal const val INSTANCES_DIR_NAME = "instances"
internal const val INSTANCE_FILE_NAME = "instance.json"

@Single
class InstanceRepositoryImpl(
    private val json: Json,
    backgroundScope: CoroutineScope,
    private val settingsRepository: SettingsRepository,
    @param:IODispatcher private val ioDispatcher: CoroutineDispatcher,
) : InstanceRepository {
    private val _instances: MutableResourceStateFlow<List<Instance>, Throwable> = MutableResourceStateFlow()
    override val instances: ResourceStateFlow<List<Instance>, Throwable> = _instances
        .onEach { logger.debug { "Instances: $it" } }
        .asResourceStateFlow(backgroundScope, false, ::reloadInstances)
    private val instancesMutex = Mutex()

    private val instancesDir: Deferred<Path> = backgroundScope.async {
        settingsRepository.globalSettings.filterIsInstance<Resource.Success<GlobalSettings>>().first()
            .let { Path(it.value.baseDir, INSTANCES_DIR_NAME) }
    }

    override suspend fun reloadInstances() {
        _instances.load(instancesMutex) {
            logger.debug { "Reloading instances" }
            withContext(ioDispatcher) {
                SystemFileSystem.createDirectories(instancesDir.await())
                SystemFileSystem.list(instancesDir.await()).map { file ->
                    async(ioDispatcher) {
                        loadInstanceDirectory(file)
                    }
                }.awaitAll().filterNotNull()
            }.also { logger.debug { "Instances reloaded" } }
        }
    }

    override suspend fun saveInstance(instance: Instance) {
        require(instance.id.isNotEmpty()) { "Instance ID cannot be empty" }
        val instanceData = instance.toData()
        withContext(ioDispatcher) {
            logger.info { "Saving instance ${instance.name} with ID ${instance.id} at location ${instance.file}" }
            SystemFileSystem.createDirectories(instance.directory)
            SystemFileSystem.sink(instance.file).buffered().use { json.encodeToSink(instanceData, it) }
        }
        _instances.addToOrUpdateList(instance) { a, b -> a.id == b.id }
    }

    override suspend fun deleteInstance(instance: Instance) {
        withContext(ioDispatcher) {
            logger.info { "Deleting instance ${instance.name} with ID ${instance.id} at location ${instance.file}" }
            SystemFileSystem.deleteRecursively(instance.directory)
        }
        _instances.removeFromList(instance) { a, b -> a.id == b.id }
    }

    private fun loadInstanceDirectory(file: Path): Instance? =
        if (SystemFileSystem.metadataOrNull(file)?.isDirectory == true) {
            logger.debug { "Loading instance directory: $file" }
            loadInstanceFile(Path(file, INSTANCE_FILE_NAME))
        } else {
            logger.warn { "Unknown file found in instance directory: $file" }
            null
        }

    private fun loadInstanceFile(file: Path): Instance? =
        if (SystemFileSystem.metadataOrNull(file)?.isRegularFile == true) {
            logger.debug { "Loading instance file: $file" }
            val instanceData = json.decodeFromSource<InstanceData>(SystemFileSystem.source(file).buffered())
            instanceData.toDomain(file.parent!!.name)
        } else {
            logger.warn { "No instance.json file found in directory: ${file.parent}" }
            null
        }

    private val Instance.directory: Path get() = Path(instancesDir.getCompleted(), id)
    private val Instance.file: Path get() = Path(directory, INSTANCE_FILE_NAME)

}



