package app.vercors.core.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.io.files.Path

suspend fun Path.linkOrCopyTo(target: Path, ioDispatcher: CoroutineDispatcher) =
    tryOrFalse { createLink(target, ioDispatcher) } ||
            tryOrFalse { createLink(target, ioDispatcher, true) } ||
            tryOrFalse { copyTo(target, ioDispatcher); true }

private suspend fun tryOrFalse(block: suspend () -> Boolean) = try {
    block()
} catch (_: Exception) {
    currentCoroutineContext().ensureActive()
    false
}

expect suspend fun Path.createLink(
    target: Path,
    ioDispatcher: CoroutineDispatcher,
    isSymbolic: Boolean = false
): Boolean
