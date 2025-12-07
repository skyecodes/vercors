package app.vercors.launcher.common.data.config

import app.vercors.launcher.common.data.AppPath
import app.vercors.launcher.common.data.IODispatcher
import app.vercors.launcher.common.data.deleteRecursively
import io.github.irgaly.kfswatch.KfsDirectoryWatcher
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource
import kotlinx.serialization.json.io.encodeToSink

private val logger = KotlinLogging.logger {}

abstract class AbstractConfigurationDataSource<T : Any>(
    private val configFileName: String,
    coroutineScope: CoroutineScope,
    @AppPath private val appPath: Path,
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
        runCatching {
            if (SystemFileSystem.exists(configFile)) {
                if (SystemFileSystem.metadataOrNull(configFile)?.isRegularFile == true) {
                    logger.debug { "Loading configuration file $configFile" }
                    return@withContext SystemFileSystem.source(configFile).buffered().use {
                        json.decodeFromSource(serializer, it)
                    }
                } else {
                    logger.warn { "Illegal file found at location $configFile" }
                    SystemFileSystem.deleteRecursively(configFile)
                }
            }
        }
        return@withContext null
    }

    suspend fun saveToFile(commonConfigurationData: T) = withContext(ioDispatcher) {
        if (SystemFileSystem.exists(configFile) && SystemFileSystem.metadataOrNull(configFile)?.isRegularFile != true) {
            logger.warn { "Illegal file found at location $configFile" }
            SystemFileSystem.deleteRecursively(configFile)
        }
        logger.debug { "Saving configuration file $configFile" }
        SystemFileSystem.createDirectories(configFile.parent!!)
        SystemFileSystem.sink(configFile).buffered().use {
            json.encodeToSink(serializer, commonConfigurationData, it)
        }
    }
}
