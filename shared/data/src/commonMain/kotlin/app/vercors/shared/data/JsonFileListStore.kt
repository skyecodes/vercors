package app.vercors.shared.data

import app.vercors.core.data.*
import app.vercors.core.domain.*
import io.github.irgaly.kfswatch.KfsDirectoryWatcher
import io.github.oshai.kotlinlogging.KMarkerFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.files.Path
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Factory

private val logger = KotlinLogging.logger {}

interface JsonFileListStore<T> {
    val state: ResourceStateFlow<Map<String, Resource<T>>>
    suspend fun write(id: String, value: T)
    suspend fun delete(id: String)
}

private class JsonFileListStoreImpl<T>(
    private val name: String,
    private val path: Path,
    private val serializer: KSerializer<T>,
    private val json: Json,
    dispatcher: CoroutineDispatcher,
    coroutineScope: CoroutineScope,
    watcher: KfsDirectoryWatcher
) : JsonFileListStore<T> {
    private val marker = KMarkerFactory.getMarker(name)
    private val ioDispatcher = dispatcher.limitedParallelism(10)
    private val mutex = Mutex()
    private val readJobMap = mutableMapOf<String, Job>()
    private val _state = MutableResourceStateFlow<Map<String, Resource<T>>>()
    override val state: ResourceStateFlow<Map<String, Resource<T>>> = _state.asResourceStateFlow(coroutineScope)

    init {
        coroutineScope.launch {
            if (!path.exists(ioDispatcher))
                path.createDirectories(dispatcher = ioDispatcher)
            watcher.add(path.toString())
            logger.debug(throwable = null, marker = marker) { "Watching $path for changes" }
            watcher.onEventFlow
                .onStart { sync() }
                .filter { it.path.isValidFileName() }
                .map { getPathFromName(it.path) }
                .collect { readFile(it) }
        }
    }

    suspend fun sync() = coroutineScope {
        _state.update { Resource.Loading(it.resultOrNull() ?: Result.Success(emptyMap())) }
        logger.debug(throwable = null, marker = marker) { "Syncing data at $path" }
        path.list(ioDispatcher)
            .filter { it.isValidFile() }
            .forEachAsync(ioDispatcher) { readFile(it) }
        _state.update { Resource.Loaded(it.resultOrThrow()) }
    }

    suspend fun readFile(path: Path) = coroutineScope {
        val id = path.name.toId()
        mutex.withLock {
            readJobMap[id]?.cancel()
            readJobMap[id] = launch {
                if (!path.isRegularFile(ioDispatcher)) {
                    _state.update { it.map { map -> map - id } }
                } else flow {
                    emit(Resource.Loading(_state.value.orNull()?.get(id)?.resultOrNull()))
                    logger.debug(throwable = null, marker = marker) { "Reading data at $path" }
                    try {
                        val data = path.readJson(serializer, json, ioDispatcher)
                        emit(Resource.Success(data))
                    } catch (e: Exception) {
                        currentCoroutineContext().ensureActive()
                        delay(1000)
                        emit(Resource.Failure(e))
                    }
                }.collect { res ->
                    _state.update { it.map { map -> map + (id to res) } }
                }
            }
        }
    }

    override suspend fun write(id: String, value: T) {
        val path = getPathFromId(id)
        if (!path.isRegularFile(ioDispatcher))
            path.deleteRecursively(dispatcher = ioDispatcher)
        logger.debug(throwable = null, marker = marker) { "Writing data at $path" }
        path.writeJson(serializer, value, json, ioDispatcher)
    }

    override suspend fun delete(id: String) {
        val path = getPathFromId(id)
        logger.debug(throwable = null, marker = marker) { "Deleting data at $path" }
        path.deleteRecursively(dispatcher = ioDispatcher)
    }

    private suspend fun Path.isValidFile() = isRegularFile(ioDispatcher) && name.isValidFileName()

    private fun String.isValidFileName() = endsWith(".json")

    private fun String.toId() = removeSuffix(".json")

    private fun getPathFromName(name: String) = Path(path, name)

    private fun getPathFromId(id: String) = getPathFromName("$id.json")
}

interface JsonFileListStoreFactory {
    operator fun <T> invoke(name: String, path: Path, serializer: KSerializer<T>): JsonFileListStore<T>
}

@Factory
fun provideJsonFileListStoreFactory(
    json: Json,
    @IODispatcher ioDispatcher: CoroutineDispatcher,
    coroutineScope: CoroutineScope,
    watcher: KfsDirectoryWatcher
): JsonFileListStoreFactory = object : JsonFileListStoreFactory {
    override fun <T> invoke(name: String, path: Path, serializer: KSerializer<T>): JsonFileListStore<T> =
        JsonFileListStoreImpl(name, path, serializer, json, ioDispatcher, coroutineScope, watcher)
}
