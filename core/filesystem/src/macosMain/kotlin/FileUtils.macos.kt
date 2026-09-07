package app.vercors.core.filesystem

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import platform.posix.chmod
import platform.posix.stat
import kotlin.coroutines.CoroutineContext
import kotlin.time.Instant

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
