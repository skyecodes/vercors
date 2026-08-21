package app.vercors.core.filesystem

import kotlinx.io.files.Path
import kotlin.coroutines.CoroutineContext
import kotlin.time.Instant

actual suspend fun Path.createLink(target: Path, context: CoroutineContext, isSymbolic: Boolean): Boolean {
    // TODO find out a way to hardlink/symlink on mingw
    return false
}

actual suspend fun Path.lastModifiedOrNull(context: CoroutineContext): Instant? {
    // TODO find out a way to read a file's last-modified time on mingw
    return null
}
