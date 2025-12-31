package app.vercors.feature.config.data.datasource

import app.vercors.core.data.deleteRecursively
import app.vercors.core.data.readJson
import app.vercors.core.data.writeJson
import app.vercors.shared.data.IODispatcher
import io.github.irgaly.kfswatch.KfsDirectoryWatcher
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger {}

abstract class AbstractConfigurationDataSource<T : Any>(
    private val configFileName: String,
    coroutineScope: CoroutineScope,
    private val appPath: Path,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val json: Json,
    private val watcher: KfsDirectoryWatcher,
    private val serializer: KSerializer<T>
) {
    internal val configFile = Path(appPath, configFileName)
    val updates: Flow<T?> = watcher.onEventFlow.filter { it.path == configFileName }.map { loadFromFile() }

    init {
        coroutineScope.launch {
            watcher.add(appPath.toString())
        }
    }

    suspend fun loadFromFile(): T? = withContext(ioDispatcher) {
        try {
            if (SystemFileSystem.exists(configFile)) {
                if (SystemFileSystem.metadataOrNull(configFile)?.isRegularFile == true) {
                    logger.info { "Loading configuration file $configFile" }
                    return@withContext configFile.readJson(serializer, json, ioDispatcher)
                } else {
                    logger.warn { "Illegal file found at location $configFile" }
                    SystemFileSystem.deleteRecursively(configFile)
                }
            }
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            logger.error(e) { "Error loading configuration file $configFile" }
        }
        return@withContext null
    }

    suspend fun saveToFile(commonConfigurationData: T) = withContext(ioDispatcher) {
        if (SystemFileSystem.exists(configFile) && SystemFileSystem.metadataOrNull(configFile)?.isRegularFile != true) {
            logger.warn { "Illegal file found at location $configFile" }
            SystemFileSystem.deleteRecursively(configFile)
        }
        logger.info { "Saving configuration file $configFile" }
        configFile.writeJson(serializer, commonConfigurationData, json, ioDispatcher)
    }
}
