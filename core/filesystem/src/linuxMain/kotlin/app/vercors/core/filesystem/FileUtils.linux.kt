package app.vercors.core.filesystem

import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import platform.posix.link
import platform.posix.symlink
import kotlin.coroutines.CoroutineContext

actual suspend fun Path.createLink(target: Path, context: CoroutineContext, isSymbolic: Boolean): Boolean {
    target.createParentDirectories()
    return withContext(context) {
        if (isSymbolic) symlink(toString(), target.toString()) == 0
        else link(toString(), target.toString()) == 0
    }
}
