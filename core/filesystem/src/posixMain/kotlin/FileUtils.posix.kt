package app.vercors.core.filesystem

import kotlinx.cinterop.*
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import platform.posix.PATH_MAX
import platform.posix.link
import platform.posix.realpath
import platform.posix.symlink
import kotlin.coroutines.CoroutineContext

actual suspend fun Path.createLink(target: Path, context: CoroutineContext, isSymbolic: Boolean): Boolean {
    target.createParentDirectories()
    // Capture path strings before withContext: its CoroutineScope receiver shadows bare toString()
    val sourcePath = toString()
    val targetPath = target.toString()
    return withContext(context) {
        if (isSymbolic) symlink(sourcePath, targetPath) == 0
        else link(sourcePath, targetPath) == 0
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
