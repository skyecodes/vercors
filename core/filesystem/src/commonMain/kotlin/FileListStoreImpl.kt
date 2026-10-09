package app.vercors.core.filesystem

import app.vercors.core.domain.AppConstants
import app.vercors.core.domain.MutableProgressiveStateFlow
import app.vercors.core.domain.forEachAsync
import arrow.core.Either
import arrow.core.raise.either
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.IOException
import kotlinx.io.files.Path
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import opensavvy.progress.done
import opensavvy.progress.loading
import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.copy
import opensavvy.state.progressive.map
import opensavvy.state.progressive.successfulWithProgress
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.time.Duration.Companion.milliseconds

private val logger = KotlinLogging.logger {}

internal class FileListStoreImpl<T>(
    private val directoryPath: Path,
    private val serializer: KSerializer<T>,
    private val json: Json,
    private val fileWatcherService: FileWatcherService,
    private val backgroundScope: CoroutineScope,
    @Named(AppConstants.PARALLEL_FILESYSTEM_OPERATIONS_KEY) private val parallelFilesystemOperationsProvider: () -> Int
) : FileListStore<T> {
    private val loadMutex = Mutex()
    private val fileWriteLock = mutableMapOf<String, Mutex>()
    private val loadMap = mutableMapOf<String, Job>()
    private var watcherJob: Job? = null
    private val _flow = MutableProgressiveStateFlow<Nothing, Map<String, Either<FileStoreError, T>>>()
    override val flow: ProgressiveFlow<Nothing, Map<String, Either<FileStoreError, T>>> =
        _flow.onStart { backgroundScope.launch { loadAll() } }

    private fun startWatcher() {
        if (watcherJob?.isActive == true) return
        watcherJob = backgroundScope.launch {
            fileWatcherService.watchDirectory(directoryPath).collect { event ->
                loadMap.put(
                    event.path.name,
                    this@launch.launch { loadOne(event) }.apply {
                        invokeOnCompletion { loadMap.remove(event.path.name) }
                    }
                )?.cancel()
            }
        }
    }

    @OptIn(ExperimentalAtomicApi::class)
    private suspend fun loadAll() {
        loadMutex.withLock {
            _flow.emit(_flow.value.copy(loading(0.0)))
            if (!directoryPath.exists()) directoryPath.createDirectories()
            startWatcher()
            val current = AtomicInt(0)
            val files = directoryPath.list().filter { it.name.endsWith(".json") }
            if (files.isEmpty()) {
                logger.debug { "No files to read from $directoryPath" }
                _flow.emit(emptyMap<String, Either<FileStoreError, T>>().successfulWithProgress())
                return
            }
            logger.debug { "Reading ${files.size} files from $directoryPath" }
            files.forEachAsync(concurrency = parallelFilesystemOperationsProvider()) { filePath ->
                val result = readFileData(filePath)
                current.incrementAndFetch()
                _flow.update {
                    val res = if (it is ProgressiveOutcome.Incomplete) mapOf(result).successfulWithProgress()
                    else it.map { list -> list + result }
                    res.copy(progress = loading(current.load() / files.size.toDouble()))
                }
            }
            _flow.update {
                when (it) {
                    is ProgressiveOutcome.Incomplete -> emptyMap<String, Either<FileStoreError, T>>().successfulWithProgress()
                    is ProgressiveOutcome.Success -> it.copy(progress = done())
                    is ProgressiveOutcome.Failure -> it.copy(progress = done())
                }
            }
        }
    }

    private suspend fun loadOne(event: FileEvent) {
        loadMutex.withLock {
            if (event.type == FileEvent.Type.DELETE) {
                _flow.update { outcome ->
                    outcome.map { it - event.path.instanceKey }
                }
            } else {
                val result = readFileData(event.path)
                _flow.update { outcome ->
                    outcome.map { it + result }
                }
            }
        }
    }

    private suspend fun readFileData(filePath: Path): Pair<String, Either<FileStoreError, T>> =
        filePath.instanceKey to either {
            try {
                logger.debug { "Reading data from $filePath" }
                filePath.readJson(serializer, json)
            } catch (e: Exception) {
                currentCoroutineContext().ensureActive()
                delay(100.milliseconds) // If another update happens within 100ms, the failure will be canceled
                logger.error(e) { "Failed to read data from $filePath" }
                when (e) {
                    is IOException -> raise(FileStoreError.IO)
                    is SerializationException -> raise(FileStoreError.Serialization)
                    else -> throw e
                }
            }
        }

    override suspend fun write(name: String, data: T) {
        val filePath = Path(directoryPath, "$name.json")
        fileWriteLock.getOrPut(name, ::Mutex).withLock {
            logger.debug { "Writing data to $filePath" }
            filePath.writeJson(serializer, data, json)
        }
    }

    override suspend fun delete(name: String) {
        val filePath = Path(directoryPath, "$name.json")
        fileWriteLock.getOrPut(name, ::Mutex).withLock {
            logger.debug { "Deleting file at $filePath" }
            filePath.delete()
        }
    }

    val Path.instanceKey: String get() = name.removeSuffix(".json")
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class FileListStoreFactoryImpl(
    private val json: Json,
    private val fileWatcherService: FileWatcherService,
    private val backgroundScope: CoroutineScope,
    @Named(AppConstants.PARALLEL_FILESYSTEM_OPERATIONS_KEY) private val parallelFilesystemOperationsProvider: () -> Int
) : FileListStoreFactory {
    override operator fun <T> invoke(directoryPath: Path, serializer: KSerializer<T>): FileListStore<T> =
        FileListStoreImpl(
            directoryPath,
            serializer,
            json,
            fileWatcherService,
            backgroundScope,
            parallelFilesystemOperationsProvider
        )
}
