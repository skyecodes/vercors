package app.vercors.core.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.io.RawSource
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource
import kotlinx.serialization.json.io.encodeToSink

@OptIn(ExperimentalSerializationApi::class)
suspend inline fun <reified T> Path.readJson(
    json: Json,
    ioDispatcher: CoroutineDispatcher
): T = withContext(ioDispatcher) {
    SystemFileSystem.source(this@readJson).buffered().use {
        json.decodeFromSource<T>(it)
    }
}

@OptIn(ExperimentalSerializationApi::class)
suspend inline fun <T> Path.readJson(
    deserializer: DeserializationStrategy<T>,
    json: Json,
    ioDispatcher: CoroutineDispatcher
): T = withContext(ioDispatcher) {
    SystemFileSystem.source(this@readJson).buffered().use {
        json.decodeFromSource(deserializer, it)
    }
}

@OptIn(ExperimentalSerializationApi::class)
suspend inline fun <reified T> Path.writeJson(
    value: T,
    json: Json,
    ioDispatcher: CoroutineDispatcher
) = withContext(ioDispatcher) {
    SystemFileSystem.createParentDirectories(this@writeJson)
    SystemFileSystem.sink(this@writeJson).buffered().use {
        json.encodeToSink<T>(value, it)
    }
}

@OptIn(ExperimentalSerializationApi::class)
suspend inline fun <T> Path.writeJson(
    serializer: SerializationStrategy<T>,
    value: T,
    json: Json,
    ioDispatcher: CoroutineDispatcher
) = withContext(ioDispatcher) {
    SystemFileSystem.createParentDirectories(this@writeJson)
    SystemFileSystem.sink(this@writeJson).buffered().use {
        json.encodeToSink(serializer, value, it)
    }
}

suspend fun Path.copyTo(
    target: Path,
    ioDispatcher: CoroutineDispatcher
) = SystemFileSystem.source(this).use { target.writeFromSource(it, ioDispatcher) }

suspend fun Path.writeFromSource(
    source: RawSource,
    ioDispatcher: CoroutineDispatcher
) = withContext(ioDispatcher) {
    SystemFileSystem.createParentDirectories(this@writeFromSource)
    SystemFileSystem.sink(this@writeFromSource).buffered().use { sink ->
        source.buffered().use { it.transferTo(sink) }
    }
}

fun FileSystem.createParentDirectories(path: Path, mustCreate: Boolean = false) {
    path.parent?.let { createDirectories(it, mustCreate) }
}

/**
 * Delete the given path, recursively if it is a directory.
 *
 * @throws kotlinx.io.files.FileNotFoundException - when [path] does not exist and [mustExist] is
 *         `true`
 * @throws kotlinx.io.IOException if there was an underlying error preventing listing the [path]'s
 *         children if it was a directory
 * */
fun FileSystem.deleteRecursively(path: Path, mustExist: Boolean = false) {
    val isDirectory = metadataOrNull(path)?.isDirectory ?: false
    if (isDirectory) {
        for (child in list(path)) {
            deleteRecursively(child, mustExist)
        }
    }
    delete(path, mustExist)
}
