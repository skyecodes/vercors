package app.vercors.shared.data

import app.vercors.core.data.deleteRecursively
import app.vercors.core.data.isRegularFile
import app.vercors.core.data.readJson
import app.vercors.core.data.writeJson
import app.vercors.core.domain.*
import io.github.irgaly.kfswatch.KfsDirectoryWatcher
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.files.FileNotFoundException
import kotlinx.io.files.Path
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

interface JsonFileState<T> {
    val state: ResourceStateFlow<T>
    suspend fun write(value: T)
}

private class JsonFileStateImpl<T>(
    private val path: Path,
    private val serializer: KSerializer<T>,
    private val defaultProvider: () -> T,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher,
    coroutineScope: CoroutineScope,
    watcher: KfsDirectoryWatcher
) : JsonFileState<T> {
    private val mutex = Mutex()
    private val _state = MutableResourceStateFlow<T>()
    override val state: ResourceStateFlow<T> = _state.asResourceStateFlow(coroutineScope)

    init {
        coroutineScope.launch {
            watcher.add(path.parent!!.toString())
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
                write(defaultProvider())
                awaitCancellation() // Wait for the update to get picked up by the watcher
            }
            _state.update { Resource.Loading(it.resultOrNull()) }
            val result = try {
                Resource.Success(path.readJson(serializer, json, ioDispatcher))
            } catch (_: FileNotFoundException) {
                write(defaultProvider())
                Resource.Unloaded
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
        path.writeJson(serializer, value, json, ioDispatcher)
    }
}

interface JsonFileStateFactory {
    operator fun <T> invoke(path: Path, serializer: KSerializer<T>, defaultProvider: () -> T): JsonFileState<T>
}

@Single
fun provideJsonFileStateFactory(
    json: Json,
    @IODispatcher ioDispatcher: CoroutineDispatcher,
    coroutineScope: CoroutineScope,
    watcher: KfsDirectoryWatcher
): JsonFileStateFactory = object : JsonFileStateFactory {
    override fun <T> invoke(path: Path, serializer: KSerializer<T>, defaultProvider: () -> T): JsonFileState<T> =
        JsonFileStateImpl(path, serializer, defaultProvider, json, ioDispatcher, coroutineScope, watcher)
}
