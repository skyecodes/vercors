package app.vercors.feature.instance.data.datasource

import app.vercors.core.data.deleteRecursively
import app.vercors.core.data.readJson
import app.vercors.core.data.writeJson
import app.vercors.feature.instance.data.dto.InstanceData
import app.vercors.shared.data.IODispatcher
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

private val logger = KotlinLogging.logger {}

internal const val INSTANCES_DIR_NAME = "instances"
internal const val INSTANCE_FILE_NAME = "instance.json"

@Single
internal class InstanceRepositoryDataSource(
    private val appPath: Path,
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
                instancesDir.deleteRecursively(dispatcher = ioDispatcher)
            }
        }
        emptyList()
    }

    private suspend fun loadFromDirectory(directory: Path): Pair<String, InstanceData>? = withContext(ioDispatcher) {
        val instanceFile = Path(directory, INSTANCE_FILE_NAME)
        try {
            if (SystemFileSystem.exists(instanceFile)) {
                if (SystemFileSystem.metadataOrNull(instanceFile)?.isRegularFile == true) {
                    logger.info { "Loading instance file $instanceFile" }
                    return@withContext directory.name to instanceFile.readJson<InstanceData>(json, ioDispatcher)
                } else {
                    logger.warn { "Unknown file found in instance directory $instanceFile" }
                    instanceFile.deleteRecursively(dispatcher = ioDispatcher)
                }
            }
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            logger.error(e) { "Error loading instance file $instanceFile" }
        }
        null
    }

    suspend fun saveToFile(instanceData: InstanceData, id: String) = withContext(ioDispatcher) {
        val instanceDir = Path(instancesDir, id)
        val instanceFile = Path(instanceDir, INSTANCE_FILE_NAME)
        if (SystemFileSystem.exists(instanceFile) && SystemFileSystem.metadataOrNull(instanceFile)?.isRegularFile != true) {
            logger.warn { "Illegal file found at location $instanceFile" }
            instanceFile.deleteRecursively(dispatcher = ioDispatcher)
        }
        logger.info { "Saving instance file $instanceFile" }
        instanceFile.writeJson(instanceData, json, ioDispatcher)
    }

    suspend fun delete(id: String) = withContext(ioDispatcher) {
        val instanceDir = Path(instancesDir, id)
        logger.debug { "Deleting instance directory: $instanceDir" }
        instanceDir.deleteRecursively(dispatcher = ioDispatcher)
    }
}
