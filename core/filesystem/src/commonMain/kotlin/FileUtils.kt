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
import kotlin.time.Instant

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
    context: CoroutineContext = Dispatchers.IO,
    mustExist: Boolean = true,
) = withContext(context) {
    SystemFileSystem.delete(this@delete, mustExist)
}

suspend inline fun Path.moveTo(
    target: Path,
    context: CoroutineContext = Dispatchers.IO
) = withContext(context) {
    SystemFileSystem.atomicMove(this@moveTo, target)
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

suspend fun Path.copyRecursively(
    target: Path,
    context: CoroutineContext = Dispatchers.IO,
    onFileCopied: (bytes: Long) -> Unit = {}
) {
    if (isDirectory(context)) {
        target.createDirectories(context)
        for (child in list(context)) {
            child.copyRecursively(Path(target, child.name), context, onFileCopied)
        }
    } else {
        onFileCopied(copyTo(target, context))
    }
}

suspend inline fun <T> Path.source(
    context: CoroutineContext = Dispatchers.IO,
    crossinline block: suspend (Source) -> T,
): T = withContext(context) {
    SystemFileSystem.source(this@source).buffered().use { block(it) }
}

suspend inline fun <T> Path.sink(
    context: CoroutineContext = Dispatchers.IO,
    append: Boolean = false,
    crossinline block: suspend (Sink) -> T,
): T = withContext(context) {
    SystemFileSystem.sink(this@sink, append).buffered().use { block(it) }
}

@OptIn(ExperimentalSerializationApi::class)
suspend inline fun <reified T> Path.readJson(
    json: Json,
    context: CoroutineContext = Dispatchers.IO
): T = source(context) { json.decodeFromSource<T>(it) }

@OptIn(ExperimentalSerializationApi::class)
suspend inline fun <T> Path.readJson(
    deserializer: DeserializationStrategy<T>,
    json: Json,
    context: CoroutineContext = Dispatchers.IO
): T = source(context) { json.decodeFromSource(deserializer, it) }

@OptIn(ExperimentalSerializationApi::class)
suspend inline fun <reified T> Path.writeJson(
    value: T,
    json: Json,
    context: CoroutineContext = Dispatchers.IO
) {
    createParentDirectories(context)
    sink(context) { json.encodeToSink<T>(value, it) }
}

@OptIn(ExperimentalSerializationApi::class)
suspend inline fun <T> Path.writeJson(
    serializer: SerializationStrategy<T>,
    value: T,
    json: Json,
    context: CoroutineContext = Dispatchers.IO
) {
    createParentDirectories(context)
    sink(context) { json.encodeToSink(serializer, value, it) }
}

suspend inline fun Path.readData(
    context: CoroutineContext = Dispatchers.IO
): ByteString = source(context) { it.readByteString() }

suspend inline fun Path.writeData(
    data: ByteString,
    context: CoroutineContext = Dispatchers.IO
) = sink(context) { it.write(data) }

suspend inline fun Path.copyTo(
    target: Path,
    context: CoroutineContext = Dispatchers.IO
): Long = source(context) { target.writeFromSource(it, context) }

suspend inline fun Path.writeFromSource(
    source: Source,
    context: CoroutineContext = Dispatchers.IO
): Long {
    createParentDirectories(context)
    return sink(context) { sink -> source.use { it.transferTo(sink) } }
}

suspend inline fun Path.writeFromSource(
    source: Source,
    crossinline onBytesWritten: (Long) -> Unit,
    context: CoroutineContext = Dispatchers.IO
): Long {
    createParentDirectories(context)
    return sink(context) { sink ->
        source.use {
            val buffer = Buffer()
            var total = 0L
            while (true) {
                val read = it.readAtMostTo(buffer, WRITE_CHUNK_SIZE)
                if (read == -1L) break
                sink.write(buffer, read)
                total += read
                onBytesWritten(read)
            }
            total
        }
    }
}

@PublishedApi
internal const val WRITE_CHUNK_SIZE = 64L * 1024

suspend inline fun Path.writeText(
    text: String,
    context: CoroutineContext = Dispatchers.IO
) {
    createParentDirectories(context)
    sink(context) { it.writeString(text) }
}

suspend inline fun Path.readText(
    context: CoroutineContext = Dispatchers.IO
): String = source(context) { it.readString() }

suspend fun Path.linkOrCopyTo(target: Path, context: CoroutineContext = Dispatchers.IO) =
    tryOrFalse { createLink(target, context) } ||
            tryOrFalse { createLink(target, context, true) } ||
            tryOrFalse { copyTo(target, context); true }

suspend fun Path.linkRecursively(target: Path, context: CoroutineContext = Dispatchers.IO) {
    if (isDirectory(context)) {
        target.createDirectories(context)
        for (child in list(context)) {
            child.linkRecursively(Path(target, child.name), context)
        }
    } else {
        linkOrCopyTo(target, context)
    }
}

suspend fun Path.sizeRecursively(context: CoroutineContext = Dispatchers.IO): Long =
    if (isDirectory(context)) list(context).sumOf { it.sizeRecursively(context) }
    else metadataOrNull(context)?.size ?: 0

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

expect suspend fun Path.lastModifiedOrNull(context: CoroutineContext = Dispatchers.IO): Instant?

expect suspend fun Path.setExecutable(context: CoroutineContext = Dispatchers.IO): Boolean

expect suspend fun Path.restrictToOwner(context: CoroutineContext = Dispatchers.IO): Boolean

expect suspend fun Path.realPath(context: CoroutineContext = Dispatchers.IO): Path?
