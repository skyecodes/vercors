package app.vercors.core.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.io.files.Path

actual suspend fun Path.createLink(target: Path, ioDispatcher: CoroutineDispatcher, isSymbolic: Boolean): Boolean {
    // TODO find out a way to hardlink/symlink on mingw
    return false
}
