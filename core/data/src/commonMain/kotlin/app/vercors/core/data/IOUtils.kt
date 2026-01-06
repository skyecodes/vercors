package app.vercors.core.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource
import kotlinx.serialization.json.io.encodeToSink

suspend inline fun Path.exists(
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): Boolean = withContext(dispatcher) {
    SystemFileSystem.exists(this@exists)
}

suspend inline fun Path.isRegularFile(
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) = withContext(dispatcher) {
    SystemFileSystem.metadataOrNull(this@isRegularFile)?.isRegularFile == true
}

suspend inline fun Path.isDirectory(
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) = withContext(dispatcher) {
    SystemFileSystem.metadataOrNull(this@isDirectory)?.isDirectory == true
}

suspend inline fun Path.list(
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): Collection<Path> = withContext(dispatcher) {
    SystemFileSystem.list(this@list)
}

suspend inline fun Path.createDirectories(
    mustCreate: Boolean = false,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) = withContext(dispatcher) {
    SystemFileSystem.createDirectories(this@createDirectories, mustCreate)
}

suspend inline fun Path.createParentDirectories(
    mustCreate: Boolean = false,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) = parent?.createDirectories(mustCreate, dispatcher)

suspend inline fun Path.delete(
    mustExist: Boolean = false,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) = withContext(dispatcher) {
    SystemFileSystem.delete(this@delete, mustExist)
}

suspend fun Path.deleteRecursively(
    mustExist: Boolean = false,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    if (isDirectory(dispatcher)) {
        for (child in list(dispatcher)) {
            child.deleteRecursively(mustExist, dispatcher)
        }
    }
    delete(mustExist, dispatcher)
}

inline fun <T> Path.source(
    block: (Source) -> T
): T = SystemFileSystem.source(this).buffered().use(block)

inline fun <T> Path.sink(
    append: Boolean = false,
    block: (Sink) -> T
): T = SystemFileSystem.sink(this, append).buffered().use(block)

@OptIn(ExperimentalSerializationApi::class)
suspend inline fun <reified T> Path.readJson(
    json: Json,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): T = source {
    withContext(dispatcher) {
        json.decodeFromSource<T>(it)
    }
}

@OptIn(ExperimentalSerializationApi::class)
suspend inline fun <T> Path.readJson(
    deserializer: DeserializationStrategy<T>,
    json: Json,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): T = source {
    withContext(dispatcher) {
        json.decodeFromSource(deserializer, it)
    }
}

@OptIn(ExperimentalSerializationApi::class)
suspend inline fun <reified T> Path.writeJson(
    value: T,
    json: Json,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    createParentDirectories()
    sink {
        withContext(dispatcher) {
            json.encodeToSink<T>(value, it)
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
suspend inline fun <T> Path.writeJson(
    serializer: SerializationStrategy<T>,
    value: T,
    json: Json,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    createParentDirectories(dispatcher = dispatcher)
    sink {
        withContext(dispatcher) {
            json.encodeToSink(serializer, value, it)
        }
    }
}

suspend inline fun Path.copyTo(
    target: Path,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) = source { target.writeFromSource(it, dispatcher) }

suspend inline fun Path.writeFromSource(
    source: Source,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    createParentDirectories(dispatcher = dispatcher)
    sink { sink ->
        source.use {
            withContext(dispatcher) {
                it.transferTo(sink)
            }
        }
    }
}

suspend inline fun Path.writeText(
    text: String,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    createParentDirectories(dispatcher = dispatcher)
    sink {
        withContext(dispatcher) {
            it.writeString(text)
        }
    }
}
