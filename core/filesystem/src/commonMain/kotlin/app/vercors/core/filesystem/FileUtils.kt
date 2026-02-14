package app.vercors.core.filesystem

import kotlinx.coroutines.*
import kotlinx.io.*
import kotlinx.io.bytestring.ByteString
import kotlinx.io.files.FileMetadata
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource
import kotlinx.serialization.json.io.encodeToSink
import kotlin.coroutines.CoroutineContext

suspend inline fun Path.exists(
    context: CoroutineContext = Dispatchers.IO
): Boolean = withContext(context) {
    SystemFileSystem.exists(this@exists)
}

suspend inline fun Path.metadataOrNull(
    context: CoroutineContext = Dispatchers.IO
): FileMetadata? = withContext(context) {
    SystemFileSystem.metadataOrNull(this@metadataOrNull)
}

suspend inline fun Path.isRegularFile(
    context: CoroutineContext = Dispatchers.IO
) = metadataOrNull(context)?.isRegularFile == true

suspend inline fun Path.isDirectory(
    context: CoroutineContext = Dispatchers.IO
) = metadataOrNull(context)?.isDirectory == true


suspend inline fun Path.list(
    context: CoroutineContext = Dispatchers.IO
): Collection<Path> = withContext(context) {
    SystemFileSystem.list(this@list)
}

suspend inline fun Path.createDirectories(
    context: CoroutineContext = Dispatchers.IO
) = withContext(context) {
    SystemFileSystem.createDirectories(this@createDirectories)
}

suspend inline fun Path.createParentDirectories(
    context: CoroutineContext = Dispatchers.IO
) = parent?.createDirectories(context)

suspend inline fun Path.delete(
    context: CoroutineContext = Dispatchers.IO
) = withContext(context) {
    SystemFileSystem.delete(this@delete)
}

suspend fun Path.deleteRecursively(
    context: CoroutineContext = Dispatchers.IO
) {
    if (isDirectory(context)) {
        for (child in list(context)) {
            child.deleteRecursively(context)
        }
    }
    delete(context)
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
    context: CoroutineContext = Dispatchers.IO
): T = source {
    withContext(context) {
        json.decodeFromSource<T>(it)
    }
}

@OptIn(ExperimentalSerializationApi::class)
suspend inline fun <T> Path.readJson(
    deserializer: DeserializationStrategy<T>,
    json: Json,
    context: CoroutineContext = Dispatchers.IO
): T = source {
    withContext(context) {
        json.decodeFromSource(deserializer, it)
    }
}

@OptIn(ExperimentalSerializationApi::class)
suspend inline fun <reified T> Path.writeJson(
    value: T,
    json: Json,
    context: CoroutineContext = Dispatchers.IO
) {
    createParentDirectories()
    sink {
        withContext(context) {
            json.encodeToSink<T>(value, it)
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
suspend inline fun <T> Path.writeJson(
    serializer: SerializationStrategy<T>,
    value: T,
    json: Json,
    context: CoroutineContext = Dispatchers.IO
) {
    createParentDirectories(context)
    sink {
        withContext(context) {
            json.encodeToSink(serializer, value, it)
        }
    }
}

suspend inline fun Path.readData(
    context: CoroutineContext = Dispatchers.IO
): ByteString = source {
    withContext(context) {
        it.readByteString()
    }
}

suspend inline fun Path.writeData(
    data: ByteString,
    context: CoroutineContext = Dispatchers.IO
) = sink {
    withContext(context) {
        it.write(data)
    }
}

suspend inline fun Path.copyTo(
    target: Path,
    context: CoroutineContext = Dispatchers.IO
): Long = source { target.writeFromSource(it, context) }

suspend inline fun Path.writeFromSource(
    source: Source,
    context: CoroutineContext = Dispatchers.IO
): Long {
    createParentDirectories(context)
    return sink { sink ->
        source.use {
            withContext(context) {
                it.transferTo(sink)
            }
        }
    }
}

suspend inline fun Path.writeText(
    text: String,
    context: CoroutineContext = Dispatchers.IO
) {
    createParentDirectories(context)
    sink {
        withContext(context) {
            it.writeString(text)
        }
    }
}

suspend fun Path.linkOrCopyTo(target: Path, context: CoroutineContext = Dispatchers.IO) =
    tryOrFalse { createLink(target, context) } ||
            tryOrFalse { createLink(target, context, true) } ||
            tryOrFalse { copyTo(target, context); true }

private suspend fun tryOrFalse(block: suspend () -> Boolean) = try {
    block()
} catch (_: Exception) {
    currentCoroutineContext().ensureActive()
    false
}

expect suspend fun Path.createLink(
    target: Path,
    context: CoroutineContext = Dispatchers.IO,
    isSymbolic: Boolean = false
): Boolean
