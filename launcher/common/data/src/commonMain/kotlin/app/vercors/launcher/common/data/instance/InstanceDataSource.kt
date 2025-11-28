package app.vercors.launcher.common.data.instance

import app.vercors.launcher.common.data.AppPath
import app.vercors.launcher.common.data.IODispatcher
import app.vercors.launcher.common.data.deleteRecursively
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource
import kotlinx.serialization.json.io.encodeToSink
import org.koin.core.annotation.Single

private val logger = KotlinLogging.logger {}

internal const val INSTANCES_DIR_NAME = "instances"
internal const val INSTANCE_FILE_NAME = "instance.json"

@Single
internal class InstanceRepositoryDataSource(
    @AppPath private val appPath: Path,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val json: Json,
) {
    private val instancesDir: Path = Path(appPath, INSTANCES_DIR_NAME)

    suspend fun loadAll(): List<Pair<String, InstanceData>> = withContext(ioDispatcher) {
        if (SystemFileSystem.exists(instancesDir)) {
            if (SystemFileSystem.metadataOrNull(instancesDir)?.isDirectory == true) {
                return@withContext SystemFileSystem.list(instancesDir).map { file ->
                    async { loadFromDirectory(file) }
                }.awaitAll().filterNotNull()
            } else {
                logger.warn { "Illegal file found at location $instancesDir" }
                SystemFileSystem.deleteRecursively(instancesDir)
            }
        }
        emptyList()
    }

    private suspend fun loadFromDirectory(directory: Path): Pair<String, InstanceData>? = withContext(ioDispatcher) {
        val instanceFile = Path(directory, INSTANCE_FILE_NAME)
        if (SystemFileSystem.exists(instanceFile)) {
            if (SystemFileSystem.metadataOrNull(instanceFile)?.isRegularFile == true) {
                logger.debug { "Loading instance file: $instanceFile" }
                return@withContext directory.name to SystemFileSystem.source(instanceFile).buffered().use {
                    json.decodeFromSource<InstanceData>(it)
                }
            } else {
                logger.warn { "Unknown file found in instance directory $instanceFile" }
                SystemFileSystem.deleteRecursively(instanceFile)
            }
        }
        null
    }

    suspend fun saveToFile(instanceData: InstanceData, id: String) = withContext(ioDispatcher) {
        val instanceDir = Path(instancesDir, id)
        val instanceFile = Path(instanceDir, INSTANCE_FILE_NAME)
        if (SystemFileSystem.exists(instanceFile) && SystemFileSystem.metadataOrNull(instanceFile)?.isRegularFile != true) {
            logger.warn { "Illegal file found at location $instanceFile" }
            SystemFileSystem.deleteRecursively(instanceFile)
        }
        logger.debug { "Saving instance file" }
        SystemFileSystem.createDirectories(instanceDir)
        SystemFileSystem.sink(instanceFile).buffered().use {
            json.encodeToSink(instanceData, it)
        }
    }

    suspend fun delete(id: String) = withContext(ioDispatcher) {
        val instanceDir = Path(instancesDir, id)
        logger.debug { "Deleting instance directory: $instanceDir" }
        SystemFileSystem.deleteRecursively(instanceDir)
    }
}