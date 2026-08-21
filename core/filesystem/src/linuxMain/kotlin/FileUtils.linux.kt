package app.vercors.core.filesystem

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import platform.posix.link
import platform.posix.stat
import platform.posix.symlink
import kotlin.coroutines.CoroutineContext
import kotlin.time.Instant

actual suspend fun Path.createLink(target: Path, context: CoroutineContext, isSymbolic: Boolean): Boolean {
    target.createParentDirectories()
    return withContext(context) {
        if (isSymbolic) symlink(toString(), target.toString()) == 0
        else link(toString(), target.toString()) == 0
    }
}

@OptIn(ExperimentalForeignApi::class)
actual suspend fun Path.lastModifiedOrNull(context: CoroutineContext): Instant? = withContext(context) {
    memScoped {
        val result = alloc<stat>()
        if (stat(toString(), result.ptr) != 0) null else Instant.fromEpochSeconds(result.st_mtim.tv_sec)
    }
}
