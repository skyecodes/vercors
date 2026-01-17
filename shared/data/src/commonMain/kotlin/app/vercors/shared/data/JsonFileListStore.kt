package app.vercors.shared.data

import app.vercors.core.data.*
import app.vercors.core.domain.*
import dev.zacsweers.metro.*
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

private val logger = KotlinLogging.logger {}

@AssistedInject
class JsonFileListStore<T>(
    @Assisted name: String,
    @Assisted private val path: Path,
    @Assisted private val serializer: KSerializer<T>,
    private val json: Json,
    coroutineScope: CoroutineScope,
    watcher: KfsDirectoryWatcher,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(10)
) {
    private val marker = KMarkerFactory.getMarker(name)
    private val mutex = Mutex()
    private val readJobMap = mutableMapOf<String, Job>()
    private val _state = MutableResourceStateFlow<Map<String, Resource<T>>>()
    val state: ResourceStateFlow<Map<String, Resource<T>>> = _state.asResourceStateFlow(coroutineScope)

    init {
        coroutineScope.launch {
            if (!path.exists())
                path.createDirectories()
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
        path.list()
            .filter { it.isValidFile() }
            .forEachAsync(dispatcher) { readFile(it) }
        _state.update { Resource.Loaded(it.resultOrThrow()) }
    }

    private suspend fun readFile(path: Path) = coroutineScope {
        val id = path.name.toId()
        readJobMap[id]?.cancel()
        mutex.withLock {
            readJobMap[id] = launch {
                if (!path.isRegularFile()) {
                    _state.update { it.map { map -> map - id } }
                } else flow {
                    emit(Resource.Loading(_state.value.orNull()?.get(id)?.resultOrNull()))
                    logger.debug(throwable = null, marker = marker) { "Reading data at $path" }
                    try {
                        val data = path.readJson(serializer, json)
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

    suspend fun write(id: String, value: T) {
        val path = getPathFromId(id)
        if (path.exists() && !path.isRegularFile())
            path.deleteRecursively()
        logger.debug(throwable = null, marker = marker) { "Writing data at $path" }
        path.writeJson(serializer, value, json)
    }

    suspend fun delete(id: String) {
        val path = getPathFromId(id)
        logger.debug(throwable = null, marker = marker) { "Deleting data at $path" }
        path.deleteRecursively()
    }

    private suspend fun Path.isValidFile() = isRegularFile() && name.isValidFileName()

    private fun String.isValidFileName() = endsWith(".json")

    private fun String.toId() = removeSuffix(".json")

    private fun getPathFromName(name: String) = Path(path, name)

    private fun getPathFromId(id: String) = getPathFromName("$id.json")

    @SingleIn(AppScope::class)
    @AssistedFactory
    fun interface Factory<T> {
        fun create(name: String, path: Path, serializer: KSerializer<T>): JsonFileListStore<T>
    }
}
