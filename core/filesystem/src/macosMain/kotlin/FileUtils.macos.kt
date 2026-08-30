package app.vercors.core.filesystem

import kotlinx.cinterop.*
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import platform.posix.*
import kotlin.coroutines.CoroutineContext
import kotlin.time.Instant

actual suspend fun Path.createLink(target: Path, context: CoroutineContext, isSymbolic: Boolean): Boolean {
    target.createParentDirectories()
    // withContext's lambda has CoroutineScope as its implicit receiver, which shadows this Path receiver for bare
    // member calls like toString() — capture both path strings first so we never call the wrong toString().
    val sourcePath = toString()
    val targetPath = target.toString()
    return withContext(context) {
        if (isSymbolic) symlink(sourcePath, targetPath) == 0
        else link(sourcePath, targetPath) == 0
    }
}

@OptIn(ExperimentalForeignApi::class)
actual suspend fun Path.lastModifiedOrNull(context: CoroutineContext): Instant? {
    val path = toString()
    return withContext(context) {
        memScoped {
            val result = alloc<stat>()
            if (stat(path, result.ptr) != 0) null else Instant.fromEpochSeconds(result.st_mtimespec.tv_sec)
        }
    }
}

actual suspend fun Path.setExecutable(context: CoroutineContext): Boolean {
    val path = toString()
    return withContext(context) {
        chmod(path, 493u /* 0755 */) == 0
    }
}

actual suspend fun Path.restrictToOwner(context: CoroutineContext): Boolean {
    val path = toString()
    return withContext(context) {
        chmod(path, 384u /* 0600 */) == 0
    }
}

@OptIn(ExperimentalForeignApi::class)
actual suspend fun Path.realPath(context: CoroutineContext): Path? {
    val path = toString()
    return withContext(context) {
        memScoped {
            val buffer = allocArray<ByteVar>(PATH_MAX)
            val resolved = realpath(path, buffer)
            resolved?.toKString()?.let { Path(it) }
        }
    }
}
