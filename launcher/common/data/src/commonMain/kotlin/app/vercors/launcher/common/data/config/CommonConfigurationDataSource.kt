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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource
import kotlinx.serialization.json.io.encodeToSink
import org.koin.core.annotation.Single

private val logger = KotlinLogging.logger {}

internal const val COMMON_CONFIG_FILE_NAME = "config-common.json"

@Single
internal class CommonConfigurationDataSource(
    coroutineScope: CoroutineScope,
    @AppPath private val appPath: Path,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val json: Json,
    private val watcher: KfsDirectoryWatcher
) {
    internal val configFile = Path(appPath, COMMON_CONFIG_FILE_NAME)
    val updates: Flow<CommonConfigurationData?> =
        watcher.onEventFlow.filter { it.path == COMMON_CONFIG_FILE_NAME }.map { loadFromFile() }

    init {
        coroutineScope.launch {
            watcher.add(appPath.toString())
        }
    }

    suspend fun loadFromFile(): CommonConfigurationData? = withContext(ioDispatcher) {
        runCatching {
            if (SystemFileSystem.exists(configFile)) {
                if (SystemFileSystem.metadataOrNull(configFile)?.isRegularFile == true) {
                    logger.debug { "Loading common configuration file" }
                    return@withContext SystemFileSystem.source(configFile).buffered().use {
                        json.decodeFromSource<CommonConfigurationData>(it)
                    }
                } else {
                    logger.warn { "Illegal file found at location $configFile" }
                    SystemFileSystem.deleteRecursively(configFile)
                }
            }
        }
        return@withContext null
    }

    suspend fun saveToFile(commonConfigurationData: CommonConfigurationData) = withContext(ioDispatcher) {
        if (SystemFileSystem.exists(configFile) && SystemFileSystem.metadataOrNull(configFile)?.isRegularFile != true) {
            logger.warn { "Illegal file found at location $configFile" }
            SystemFileSystem.deleteRecursively(configFile)
        }
        logger.debug { "Saving common configuration file" }
        SystemFileSystem.createDirectories(configFile.parent!!)
        SystemFileSystem.sink(configFile).buffered().use {
            json.encodeToSink(commonConfigurationData, it)
        }
    }
}