package app.vercors.core.filesystem

import app.vercors.core.domain.MutableProgressiveStateFlow
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.IOException
import kotlinx.io.files.Path
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import opensavvy.progress.loading
import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.progressive.copy
import opensavvy.state.progressive.failedWithProgress
import opensavvy.state.progressive.successfulWithProgress
import kotlin.time.Duration.Companion.milliseconds

private val logger = KotlinLogging.logger {}

internal class FileStoreImpl<T>(
    private val filePath: Path,
    private val serializer: KSerializer<T>,
    private val defaultProvider: (() -> T)?,
    private val json: Json,
    private val fileWatcherService: FileWatcherService,
    private val backgroundScope: CoroutineScope,
) : FileStore<T> {
    private val fileWriteLock = Mutex()
    private var watcherJob: Job? = null
    private val _flow = MutableProgressiveStateFlow<FileStoreError, T>()
    override val flow: ProgressiveFlow<FileStoreError, T> = _flow.onStart { backgroundScope.launch { load() } }

    private suspend fun startWatcher() {
        if (watcherJob?.isActive == true) return
        filePath.createParentDirectories()
        watcherJob = backgroundScope.launch {
            fileWatcherService.watchFile(filePath).collectLatest { load() }
        }
    }

    private suspend fun load() {
        startWatcher()
        _flow.emit(_flow.value.copy(loading()))
        try {
            if (!filePath.isRegularFile() && defaultProvider != null) {
                if (filePath.exists()) filePath.deleteRecursively()
                val data = defaultProvider()
                write(data)
                _flow.emit(data.successfulWithProgress())
            } else {
                logger.debug { "Reading data from $filePath" }
                val file = filePath.readJson(serializer, json)
                _flow.emit(file.successfulWithProgress())
            }
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            delay(100.milliseconds) // If another update happens within 100ms, the failure will be canceled
            logger.error(e) { "Failed to read data from $filePath" }
            val error = when (e) {
                is IOException -> FileStoreError.IO
                is SerializationException -> FileStoreError.Serialization
                else -> throw e
            }
            _flow.emit(error.failedWithProgress())
        }
    }

    override suspend fun write(data: T) {
        fileWriteLock.withLock {
            logger.debug { "Writing data to $filePath" }
            filePath.writeJson(serializer, data, json)
        }
    }

}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class FileStoreFactoryImpl(
    private val json: Json,
    private val fileWatcherService: FileWatcherService,
    private val backgroundScope: CoroutineScope,
) : FileStoreFactory {
    override fun <T> invoke(filePath: Path, serializer: KSerializer<T>, defaultProvider: (() -> T)?): FileStore<T> =
        FileStoreImpl(filePath, serializer, defaultProvider, json, fileWatcherService, backgroundScope)
}
