package app.vercors.shared.data

import app.vercors.core.data.deleteRecursively
import app.vercors.core.data.isRegularFile
import app.vercors.core.data.readJson
import app.vercors.core.data.writeJson
import app.vercors.core.domain.*
import io.github.irgaly.kfswatch.KfsDirectoryWatcher
import io.github.oshai.kotlinlogging.KMarkerFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.files.Path
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger {}

interface JsonFileStore<T> {
    val state: ResourceStateFlow<T>
    suspend fun write(value: T)
}

internal class JsonFileStoreImpl<T>(
    private val name: String,
    private val path: Path,
    private val serializer: KSerializer<T>,
    private val defaultProvider: () -> T,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher,
    coroutineScope: CoroutineScope,
    watcher: KfsDirectoryWatcher
) : JsonFileStore<T> {
    private val marker = KMarkerFactory.getMarker(name)
    private val mutex = Mutex()
    private val _state = MutableResourceStateFlow<T>()
    override val state: ResourceStateFlow<T> = _state.asResourceStateFlow(coroutineScope)

    init {
        coroutineScope.launch {
            watcher.add(path.parent!!.toString())
            logger.debug(throwable = null, marker = marker) { "Watching $path for changes" }
            watcher.onEventFlow
                .filter { it.path == path.name }
                .onStart { load() }
                .collectLatest { load() }
        }
    }

    private suspend fun load() {
        if (mutex.isLocked) return
        mutex.withLock {
            if (!path.isRegularFile(ioDispatcher)) {
                logger.debug(throwable = null, marker = marker) { "No file found at $path - writing default data" }
                path.deleteRecursively(dispatcher = ioDispatcher)
                write(defaultProvider())
                return
            }
            _state.update { Resource.Loading(it.resultOrNull()) }
            logger.debug(throwable = null, marker = marker) { "Reading data at $path" }
            val result = try {
                Resource.Success(path.readJson(serializer, json, ioDispatcher))
            } catch (e: Exception) {
                currentCoroutineContext().ensureActive()
                delay(1000) // If another update happens within 1 second, the failure will be canceled
                Resource.Failure(e)
            }
            _state.emit(result)
        }
    }

    override suspend fun write(value: T) {
        if (!path.isRegularFile(ioDispatcher))
            path.deleteRecursively(dispatcher = ioDispatcher)
        logger.debug(throwable = null, marker = marker) { "Writing data at $path" }
        path.writeJson(serializer, value, json, ioDispatcher)
    }
}

interface JsonFileStoreFactory {
    operator fun <T> invoke(
        name: String,
        path: Path,
        serializer: KSerializer<T>,
        defaultProvider: () -> T
    ): JsonFileStore<T>
}
