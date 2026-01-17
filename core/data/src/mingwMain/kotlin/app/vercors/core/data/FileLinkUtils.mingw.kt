package app.vercors.core.data

import kotlinx.io.files.Path
import kotlin.coroutines.CoroutineContext

actual suspend fun Path.createLink(target: Path, context: CoroutineContext, isSymbolic: Boolean): Boolean {
    // TODO find out a way to hardlink/symlink on mingw
    return false
}
