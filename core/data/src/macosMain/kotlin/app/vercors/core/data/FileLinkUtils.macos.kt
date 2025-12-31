package app.vercors.core.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import platform.posix.link
import platform.posix.symlink

actual suspend fun Path.createLink(target: Path, ioDispatcher: CoroutineDispatcher, isSymbolic: Boolean): Boolean =
    withContext(ioDispatcher) {
        SystemFileSystem.createParentDirectories(target)
        if (isSymbolic) symlink(toString(), target.toString()) == 0
        else link(toString(), target.toString()) == 0
    }
